package kafeihu.zk.database.pool;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.util.DBUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 销毁物理连接原因
 *
 *
 */
enum Reason4DestroyDBConnection
{
    /**
     * 连接无效
     */
    InvalidConn,
    /**
     * 连接空闲时间超时
     */
    IdleTimeout,
    /**
     * 连接返回连接池失败
     */
    ReturnToPoolHeadFailed,
    /**
     * 连接返回连接池失败
     */
    ReturnToPoolTailFailed,

    /**
     * 新连接插入连接池失败
     */
    AddToPoolFailed
}

/**
 * 数据库连接池
 *
 * Created by zhangkuo on 2016/11/22.
 */
public class DBConnectionPool {

    // 连接池配置
    private final DBConnectionPoolConfig poolConfig;
    // 基于链表的双向阻塞队列，数据库连接对象容器
    private LinkedBlockingDeque<PooledDBConnection> pooledDBConnContainer;
    // 数据库连接容器，用于连接池关闭时释放数据库连接资源
    // private ArrayBlockingQueue<Connection> dbConnRepo;
    // 后台定时调度线程池：过期对象清理任务调度
    private ScheduledExecutorService scheduledExecutor_ClearIdle = Executors
            .newScheduledThreadPool(1);
    // 允许同时存在的构造新连接线程数
    private Semaphore createThreadPermit;
    // 允许等待获取连接的客户数
    private Semaphore waitClientPermit;
    // 当前活跃（正在使用中的）连接数
    private AtomicInteger activeConnNum = new AtomicInteger(0);
    // 累计创建的连接数
    private AtomicLong createdConnCount = new AtomicLong(0);
    // 累计销毁的连接数
    private AtomicLong destroyedConnCount = new AtomicLong(0);
    // 获取连接失败次数
    private AtomicLong noFreeConnCount = new AtomicLong();
    // 上一次无空闲连接发生时间
    private volatile long lastNoFreeConnTime = 0L;
    // 连接池满发生次数
    private AtomicLong poolIsFullCount = new AtomicLong();
    // 上一次连接池满发生时间
    private volatile long lastPoolIsFullTime = 0L;

    // 连接池关闭标志
    private volatile boolean closed = false;
    // 日志处理器
    private Logger logger = Logger.getConsoleLogger();
    private String logTips = getClass().getName();

    public DBConnectionPool(DBConnectionPoolConfig poolConfig, Logger logger)
            throws Exception
    {
        this(poolConfig);
        setLogger(logger);
    }

    public DBConnectionPool(DBConnectionPoolConfig poolConfig) throws Exception
    {
        super();
        poolConfig.checkConfigData();
        this.poolConfig = poolConfig;
    }

    public String getID()
    {
        return poolConfig.getId();
    }

    public void setLogger(Logger logger)
    {
        if (null != logger)
        {
            this.logger = logger;
        }
    }

    public Logger getLogger()
    {
        return logger;
    }

    /**
     * 初始化连接池。使用连接池前必须首先调用该方法
     */
    public void init() throws Exception
    {
        // 连接持有者容器
        pooledDBConnContainer = new LinkedBlockingDeque<PooledDBConnection>(
                poolConfig.getMaxSize() + 1);
        // 信号量。控制后台构造新连接的线程数量
        createThreadPermit = new Semaphore(poolConfig.getMaxCreateThreadCount());
        // 信号量。控制等待中的客户数量
        waitClientPermit = new Semaphore(poolConfig.getMaxSize());
        // 构造物理数据库连接
        int initSize = poolConfig.getInitialSize();
        for (int i = 0; i < initSize; i++)
        {
            createRealConnection();
        }
        // 如果最大资源数大于最小资源数，启动定时任务，定期扫描并销毁多余的空闲连接
        if (poolConfig.getMaxSize() > poolConfig.getMinSize())
        {
            schedulePeriodTask_cleanIdle();
        }
    }

    /**
     * 启动定时任务，定期扫描并销毁多余的空闲连接
     */
    private void schedulePeriodTask_cleanIdle()
    {
        Runnable task = new Runnable()
        {
            public void run()
            {
                int cleanCnt = 0;
                logger.debug(logTips, "cleanIdleConnection task begin...");
                while (true)
                {
                    // 当前超过最小连接数配置的连接数量
                    int extConnSize = getAliveNum() - poolConfig.getMinSize();
                    if (extConnSize <= 0)
                    {
                        return;
                    }
                    for (int i = 0; i < extConnSize; i++)
                    {
                        // 从队列尾部获取一个连接
                        PooledDBConnection pooledDBConn = pooledDBConnContainer
                                .pollLast();
                        if (null == pooledDBConn)
                        {
                            logger.debug(logTips,
                                    "cleanIdleConnection task end. cleanCnt:"
                                            + cleanCnt);
                            return;
                        }
                        // 判断是否为空闲连接（空闲时间超长）
                        if (isIdleConnection(pooledDBConn))
                        {
                            // 连接空闲时间超时，删除空闲连接
                            destroyRealConnection(pooledDBConn,
                                    Reason4DestroyDBConnection.IdleTimeout);
                            cleanCnt++;
                        }
                        else
                        {
                            // 非空闲连接，插入尾部
                            if (!pooledDBConnContainer.offerLast(pooledDBConn))
                            {
                                // 插入失败，销毁连接。一般不会执行到这里
                                destroyRealConnection(
                                        pooledDBConn,
                                        Reason4DestroyDBConnection.ReturnToPoolTailFailed);
                                cleanCnt++;
                            }
                            logger.debug(logTips,
                                    "cleanIdleConnection task end. cleanCnt:"
                                            + cleanCnt);
                            // 结束扫描。数据库连接从头部获取，如果最后一个未达到空闲超时，则其他也都不会
                            return;
                        }
                    }
                }
            }
        };
        Random rand = new Random();
        int initialDelay = 10 + rand.nextInt(10);
        // 定时调度清理线程
        scheduledExecutor_ClearIdle.scheduleWithFixedDelay(task, initialDelay,
                poolConfig.getIdleConnScanSpanSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 构造一个新的物理数据库连接，并加入连接池
     *
     * @return
     */
    private void createRealConnection()
    {

        Connection realDBConn = null;
        try
        {
            // 构造新的数据库连接
            realDBConn = DriverManager.getConnection(poolConfig.getJdbcUrl(),
                    poolConfig.getUsername(), poolConfig.getPassword());
        }
        catch (Exception exp)
        {
            logger.error(logTips, "createRealConnection failed. exception:"
                    + exp.getMessage());
        }
        if (null == realDBConn)
        {
            return;
        }

        createdConnCount.incrementAndGet();
        PooledDBConnection pooledDBConn = new PooledDBConnection(realDBConn,
                this);

        if (pooledDBConnContainer.offerFirst(pooledDBConn))
        {
            // 加入连接池成功，写日志
            logger.debug(
                    logTips,
                    "createRealConnection ok. CreatedCnt:"
                            + createdConnCount.get() + ". Conn:"
                            + realDBConn.toString());
        }
        else
        {
            // 加入连接池失败，销毁数据库连接。通常不会执行到这里
            destroyRealConnection(pooledDBConn,
                    Reason4DestroyDBConnection.AddToPoolFailed);
        }

    }

    /**
     * 销毁指定持有者拥有的真正的数据库连接
     *
     * @param
     */
    private void destroyRealConnection(PooledDBConnection pooledDBConn,
                                       Reason4DestroyDBConnection reason)
    {
        // 关闭真正的数据库连接
        Connection realDBConn = pooledDBConn.getRealDBConnection();
        DBUtil.closeConnection(realDBConn);
        destroyedConnCount.incrementAndGet();
        // 从连接池中移除连接实例
        pooledDBConnContainer.remove(pooledDBConn);
        logger.info(logTips, "destroyRealConnection ok. Reason:" + reason
                + ". DestroyedCount:" + destroyedConnCount.get() + ". Conn:"
                + pooledDBConn.toString());

    }

    /**
     * 连接池是否已满
     *
     * @return
     */
    private boolean isFull()
    {
        return getAliveNum() >= poolConfig.getMaxSize();
    }

    /**
     * 启动后台线程，构造新的数据库连接
     */
    private void startCreateRealConnectionThread()
    {
        // 如果连接池已满，直接返回
        if (isFull())
        {
            return;
        }
        // 获取构造许可，避免启动过多的数据库连接后台构造线程
        if (createThreadPermit.tryAcquire())
        {
            // 启动后台线程，构造新的数据库连接并加入队列容器
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // 如果连接池已满，直接返回
                        if (isFull())
                        {
                            return;
                        }
                        createRealConnection();
                    }
                    finally
                    {
                        createThreadPermit.release();
                    }
                }
            });
            t.start();
        }

    }

    /**
     * 当前连接空闲（idle)时间是否已超过允许的最大空闲时间
     *
     * @param pooledDBConn
     * @return
     */
    private boolean isIdleConnection(PooledDBConnection pooledDBConn)
    {
        long idleTime = System.currentTimeMillis()
                - pooledDBConn.getLastAccessEndMillis();
        return (idleTime > (poolConfig.getMaxIdleSeconds() * 1000));
    }

    /**
     * 获取数据库连接
     *
     * @return 一个数据库连接实例
     * @throws DBPoolException 获取数据库连接失败：无空闲连接或连接池已关闭
     */
    public Connection getDBConnection() throws DBPoolException
    {
        if (closed)
        {
            // 连接池已关闭，返回空连接
            throw new DBPoolException("util Pool is closed");
        }
        // 从队列头部获取连接
        PooledDBConnection pooledDBConn = pooledDBConnContainer.pollFirst();
        if (null != pooledDBConn)
        {
            // 活跃连接数加一
            activeConnNum.incrementAndGet();
            pooledDBConn.beforeAccess();
            // 返回连接
            return pooledDBConn;
        }

        // dumpInfo("getDBConnection");

        if (isFull())
        {
            noFreeConnCount.incrementAndGet();
            poolIsFullCount.incrementAndGet();
            lastPoolIsFullTime = System.currentTimeMillis();
            lastNoFreeConnTime = System.currentTimeMillis();
            logger.warn(logTips,
                    "no free util available : util Pool is full");
            // 连接池已满，且无法获取空闲连接
            throw new DBPoolException(
                    "no free util available : util Pool is full");
        }

        // 连接池未满，启动后台线程构造新的连接
        startCreateRealConnectionThread();
        // 再次尝试获取连接
        if (waitClientPermit.tryAcquire())
        {
            try
            {
                pooledDBConn = pooledDBConnContainer.pollFirst(
                        poolConfig.getMaxWaitTimeoutMills(),
                        TimeUnit.MILLISECONDS);
            }
            catch (Exception exp)
            {
            }
            finally
            {
                waitClientPermit.release();
            }
        }
        if (null == pooledDBConn)
        {
            noFreeConnCount.incrementAndGet();
            lastNoFreeConnTime = System.currentTimeMillis();
            logger.warn(logTips,
                    "no free util available : get util failed");
            // 无法获取空闲连接。返回空连接
            throw new DBPoolException(
                    "no free util available : get util failed");
        }
        else
        {
            // 活跃连接数加一
            activeConnNum.incrementAndGet();
            pooledDBConn.beforeAccess();
            return pooledDBConn;
        }
    }

    /**
     * 将指定连接返回连接池
     *
     * @param pooledDBConn
     */
    public void returnDBConnection(PooledDBConnection pooledDBConn)
    {
        // 活跃连接数减一
        activeConnNum.decrementAndGet();
        if (pooledDBConn.isValid())
        {
            // 将连接插入队列头部
            if (!pooledDBConnContainer.offerFirst(pooledDBConn))
            {
                // 队列插入失败，销毁连接。通常不会走到这一步
                destroyRealConnection(pooledDBConn,
                        Reason4DestroyDBConnection.ReturnToPoolHeadFailed);
            }
        }
        else
        {
            // 连接已无效，直接销毁
            destroyRealConnection(pooledDBConn,
                    Reason4DestroyDBConnection.InvalidConn);
        }
    }

    /**
     * 判断数据库连接是否有效
     *
     * @param pooledDBConn
     * @return
     */
    public boolean isValidDBConnection(PooledDBConnection pooledDBConn)
    {
        PreparedStatement pstStatement_Query = null;
        Connection realDBConn = pooledDBConn.getRealDBConnection();
        try
        {
            String select_Sql = poolConfig.getValidationQuerySql();
            pstStatement_Query = realDBConn.prepareStatement(select_Sql);
            pstStatement_Query.executeQuery();
            return true;
        }
        catch (Exception exp)
        {
            // 发生任何异常都认为数据库连接无效
            logger.warn(logTips, "invalid util:" + realDBConn.toString()
                    + " exp:" + exp.getMessage());
        }
        finally
        {
            DBUtil.closeSqlStatement(pstStatement_Query);
        }
        return false;
    }

    public long getConnAccessSpanWarningMills()
    {
        return poolConfig.getAccessSpanWarningMills();
    }

    public boolean isMonitorSwitchOn()
    {
        return poolConfig.isMonitorSwitchOn();
    }

    /**
     * 连接池最大连接数
     *
     * @return
     */
    public int getMaxSize()
    {
        return poolConfig.getMaxSize();
    }

    /**
     * 连接池最小连接数
     *
     * @return
     */
    public int getMinSize()
    {
        return poolConfig.getMinSize();
    }

    /**
     * 当前连接池中空闲连接数
     *
     * @return
     */
    public int getIdleNum()
    {
        return pooledDBConnContainer.size();
    }

    /**
     * 当前存活的连接数：空闲的连接数+活跃的连接数
     *
     * @return
     */
    public int getAliveNum()
    {
        return getIdleNum() + getActiveNum();
    }

    /**
     * 当前活跃的（正在被使用的）连接数
     *
     * @return
     */
    public int getActiveNum()
    {
        return activeConnNum.get();
    }

    /**
     * 累计创建的物理连接数
     *
     * @return
     */
    public long getCreatedCount()
    {
        return createdConnCount.get();
    }

    /**
     * 累计销毁的物理连接数
     *
     * @return
     */
    public long getDestroyedCount()
    {
        return destroyedConnCount.get();
    }

    /**
     * 累计无空闲连接（连接池忙）次数
     *
     * @return
     */
    public long getNoFreeConnCount()
    {
        return noFreeConnCount.get();
    }

    /**
     * 最近一次无空闲连接（连接池忙）发生时间
     *
     * @return
     */
    public long getLastNoFreeConnTime()
    {
        return lastNoFreeConnTime;
    }

    /**
     * 累计连接池满次数
     *
     * @return
     */
    public long getPoolIsFullCount()
    {
        return poolIsFullCount.get();
    }

    /**
     * 最近一次连接池满发生时间
     *
     * @return
     */
    public long getLastPoolIsFullTime()
    {
        return lastPoolIsFullTime;
    }

    /**
     * 关闭连接池：销毁所有的数据库连接。<br>
     * 通常在应用系统关闭时调用该方法，来回收数据库连接资源。
     */
    public void close()
    {

        closed = true;
        // 关闭所有的数据库连接
        PooledDBConnection[] dbConnArray = pooledDBConnContainer
                .toArray(new PooledDBConnection[]
                        {});
        for (PooledDBConnection pooledDBConnection : dbConnArray)
        {
            DBUtil.closeConnection(pooledDBConnection.getRealDBConnection());
        }
        pooledDBConnContainer.clear();

        try
        {
            scheduledExecutor_ClearIdle.shutdown();
        }
        catch (Exception exp)
        {
        }
    }

    @Override
    public String toString()
    {
        return " [id=" + poolConfig.getId()
                + ", username="+ poolConfig.getUsername()
                + ", jdbcUrl="+ poolConfig.getJdbcUrl()
                + ", maxSize="+ poolConfig.getMaxSize()
                + ", minSize="+ poolConfig.getMinSize()
                + "]";
    }

    public void dumpInfo()
    {
        dumpInfo("");
    }

    public void dumpInfo(String tag)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(tag);
        sb.append(" ActiveNum:").append(getActiveNum());
        sb.append(" IdleNum:").append(getIdleNum());
        sb.append(" AliveNum:").append(getAliveNum());
        sb.append(" AliveNum:").append(
                createdConnCount.get() - destroyedConnCount.get());
        sb.append(" CreateCount:").append(createdConnCount.get());
        sb.append(" DestroyCount:").append(destroyedConnCount.get());

        System.out.println(sb.toString());
    }
}
