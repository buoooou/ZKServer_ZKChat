package kafeihu.zk.base.pool.impl;

import kafeihu.zk.base.pool.ObjectPool;
import kafeihu.zk.base.pool.ObjectPoolConfig;
import kafeihu.zk.base.pool.PoolableObjectFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 *
 * 基于Queue(LinkedBlockingDeque)的对象池。用于缓存对象。如：Socket长连接，数据库长连接等<br>
 *
 *
 * Created by zhangkuo on 2017/6/17.
 */
public class QueueBasedObjectPool implements ObjectPool {
    /**
     * 对象工厂
     */
    private PoolableObjectFactory m_objectFactory;
    /**
     * 核心空闲对象池
     */
    private BlockingQueue<Object> m_coreIdleObjectPool;
    /**
     * 扩展（超出coreSize的）空闲对象池
     */
    private BlockingQueue<Object> m_extIdleObjectPool;
    /**
     * 已借出对象池
     */
    private BlockingQueue<Object> m_activeObjectPool;
    /**
     * 对象池配置
     */
    private ObjectPoolConfig m_poolConfig;

    /**
     * 对象被访问时间
     */
    private Map<Object, Long> m_objectAccessTime = new ConcurrentHashMap<Object, Long>();

    /**
     * 定时调度线程池：过期对象清理任务调度
     */
    private ScheduledExecutorService m_scheduledExecutor_Rundt = Executors.newScheduledThreadPool(1);
    /**
     * 定时调度线程池：KeepAlive任务调度
     */
    private ScheduledExecutorService m_scheduledExecutor_KeepAlive = Executors.newScheduledThreadPool(1);

    /**
     * 构造函数
     *
     * @param mObjectFactory
     *            对象工厂
     * @param mPoolConfig
     *            资源池配置
     * @throws Exception
     */
    public QueueBasedObjectPool(PoolableObjectFactory mObjectFactory, ObjectPoolConfig mPoolConfig)
            throws Exception
    {
        super();
        m_objectFactory = mObjectFactory;
        m_poolConfig = mPoolConfig;
        if (m_poolConfig.getCoreSize() < 1)
        {
            throw new Exception("illegal pool size : coreSize <1 ");
        }

        if (m_poolConfig.getMaxSize() < m_poolConfig.getCoreSize())
        {
            throw new Exception("illegal pool size : maxSize<coreSize ");
        }
        initPool();
        // 最大资源数大于最小资源数，启动定时任务清理多余对象
        if (m_poolConfig.getMaxSize() > m_poolConfig.getCoreSize())
        {
            schedulePeriodTask_cleanRundtObject();
        }
        // 启动定时任务保持对象激活
        if (m_poolConfig.isKeepPoolableObjectActive())
        {
            schedulePeriodTask_activateObject();
        }
    }

    /**
     * 初始化接口
     *
     * @throws Exception
     */
    private void initPool() throws Exception
    {
        int coreSize = m_poolConfig.getCoreSize();
        int maxSize = m_poolConfig.getMaxSize();
        int extSize = maxSize - coreSize;
        if (extSize < 1)
        {
            extSize = 1;
        }

        Comparator<Object> comp = new PoolableObjectComparator<Object>();
        m_coreIdleObjectPool = new ArrayBlockingQueue<Object>(coreSize);
        m_extIdleObjectPool = new PriorityBlockingQueue<Object>(extSize, comp);

        m_activeObjectPool = new ArrayBlockingQueue<Object>(maxSize);
        try
        {
            for (int i = 0; i < coreSize; i++)
            {
                addObject();
            }
        }
        catch (Exception exp)
        {
            try
            {
                close();
            }
            catch (Exception e)
            {
            }
            throw exp;
        }
    }

    /**
     * 启动定时器，定期激活资源池对象
     */
    private void schedulePeriodTask_activateObject()
    {
        Runnable task = new Runnable()
        {
            public void run()
            {
                int loopCnt = m_poolConfig.getActivateObjectLoopCnt();
                for (int i = 0; i < loopCnt; i++)
                {
                    Object obj = null;
                    try
                    {
                        obj = borrowObject();
                        m_objectFactory.activateObject(obj);
                        // System.out.println(MiscUtil.getTimestamp()+" activate"+obj.toString());
                    }
                    catch (NoSuchElementException exp)
                    {
                        break;
                    }
                    catch (Exception exp)
                    {
                        if (null != obj)
                        {
                            // 如果发生异常，则对象可能已处于无效状态
                            if (!m_objectFactory.validateObject(obj))
                            {
                                // 对象已处于无效状态，销毁对象
                                invalidateObject(obj);
                                // 将对象置为null，避免重复返回资源池
                                obj = null;
                                // 此处不再增加一个新对象
                            }
                        }
                    }
                    finally
                    {
                        if (null != obj)
                        {
                            // 返回对象
                            try
                            {
                                returnObject(obj);
                            }
                            catch (Exception exp)
                            {
                            }
                        }
                    }
                }
            }
        };
        Random rand = new Random();
        int initialDelay = 10+rand.nextInt(100);
        // 定时调度对象激活线程
        m_scheduledExecutor_KeepAlive.scheduleWithFixedDelay(task, initialDelay, m_poolConfig
                .getActivateObjectIntervalSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 启动定时器，定期清理多余的对象
     */
    private void schedulePeriodTask_cleanRundtObject()
    {
        Runnable task = new Runnable()
        {
            public void run()
            {
                // System.out.println(MiscUtil.getTimestamp()+" clean RundtObject");
                int extObjNum = m_extIdleObjectPool.size();
                if (extObjNum > 0)
                {
                    List<Object> overdueObjList = new ArrayList<Object>();
                    long now = System.currentTimeMillis();
                    long keepAliveMills = m_poolConfig.getKeepAliveSeconds() * 1000L;
                    Iterator<Object> iter = m_objectAccessTime.keySet().iterator();
                    while (iter.hasNext() && (extObjNum > 0))
                    {
                        Object object = iter.next();
                        Long lastAccessTime = m_objectAccessTime.get(object);
                        if ((now - lastAccessTime.longValue()) > keepAliveMills)
                        {
                            extObjNum--;
                            overdueObjList.add(object);
                        }
                    }
                    if (overdueObjList.size() > 0)
                    {
                        synchronized (QueueBasedObjectPool.this)
                        {
                            for (Object overdueObj : overdueObjList)
                            {
                                try
                                {
                                    m_objectAccessTime.remove(overdueObj);
                                    if (m_extIdleObjectPool.remove(overdueObj))
                                    {
                                        m_objectFactory.destroyObject(overdueObj);
                                    }
                                }
                                catch (Exception exp)
                                {
                                }
                            }
                        }
                        overdueObjList.clear();
                    }
                }
            }
        };
        Random rand = new Random();
        int initialDelay = 10+rand.nextInt(100);
        // 定时调度清理线程
        m_scheduledExecutor_Rundt.scheduleWithFixedDelay(task, initialDelay, m_poolConfig.getKeepAliveSeconds(),
                TimeUnit.SECONDS);
    }

    /**
     * 从对象资源池获取对象。
     *
     *
     * @throws NoSuchElementException
     *             如果连接池对象已全部耗尽，抛出该异常
     * @throws Exception
     *             其他异常，如构造新的对象时发生异常
     * @return 资源对象实例
     */
    public synchronized Object borrowObject() throws NoSuchElementException, Exception
    {
        return borrowObject(m_poolConfig.getBorrowWaitMills());
    }

    /**
     * 从对象资源池获取对象。
     *
     * @param waitMills
     *            等待时间
     *
     * @throws NoSuchElementException
     *             如果连接池对象已全部耗尽，抛出该异常
     * @throws Exception
     *             其他异常，如构造新的对象时发生异常
     * @return 资源对象实例
     */
    public synchronized Object borrowObject(long waitMills) throws NoSuchElementException,
            Exception
    {
        // 从核心对象池获取对象
        Object obj = m_coreIdleObjectPool.poll(waitMills, TimeUnit.MILLISECONDS);
        if (null == obj)
        {
            // 从扩展对象池获取对象
            obj = m_extIdleObjectPool.poll();
        }
        if (null != obj)
        {
            // 对象插入活跃对象池
            if (m_activeObjectPool.offer(obj))
            {
                return obj;
            }
            else
            {
                // 对象插入活跃对象池失败，将对象返回对象池
                returnActiveObject(obj);
                throw new Exception("offer activeObject failed");
            }
        }
        // 如果资源池对象总数小于连接池最大值，新增对象到对象池
        while ((m_coreIdleObjectPool.size() + m_extIdleObjectPool.size() + m_activeObjectPool
                .size()) < m_poolConfig.getMaxSize())
        {
            // 新增对象到对象池
            addObject();
            // 再次获取对象
            Object newobj = m_coreIdleObjectPool.poll();
            if (null == newobj)
            {
                newobj = m_extIdleObjectPool.poll();
            }
            if (null != newobj)
            {
                if (m_activeObjectPool.offer(newobj))
                {
                    return newobj;
                }
                else
                {
                    // 对象插入活跃对象池失败，将对象返回对象池
                    returnActiveObject(newobj);
                    throw new Exception("offer new activeObject failed");
                }
            }
        }
        throw new NoSuchElementException("borrowObject() failed. meet maxSize:"
                + m_poolConfig.getMaxSize());
    }

    /**
     * 关闭对象池，销毁池中所有对象
     *
     * @throws Exception
     */
    public synchronized void close() throws Exception
    {
        try
        {
            m_scheduledExecutor_Rundt.shutdown();
            m_scheduledExecutor_KeepAlive.shutdown();
        }
        catch (Exception exp)
        {
        }
        // 销毁连接池中的对象
        closeObjectPool(m_coreIdleObjectPool);
        closeObjectPool(m_extIdleObjectPool);
        // 销毁活跃对象
        closeObjectPool(m_activeObjectPool);

    }

    private void closeObjectPool(BlockingQueue<Object> pool)
    {
        Collection<Object> col = new ArrayList<Object>();
        pool.drainTo(col);
        for (Object obj : col)
        {
            try
            {
                m_objectFactory.destroyObject(obj);
                obj = null;
            }
            catch (Exception e)
            {
            }
        }
        col.clear();
    }

    /**
     * 将对象返回资源池
     *
     * @param obj
     * @throws Exception
     */
    public synchronized void returnObject(Object obj) throws Exception
    {
        // 从活跃对象池移除
        if(m_activeObjectPool.remove(obj))
        {
            //规划活跃对象到对象池
            returnActiveObject(obj);
        }
        else
        {
            // 从活跃对象池移除，直接销毁对象。通常应该不会执行到此处
            try
            {
                m_objectFactory.destroyObject(obj);
            }
            catch (Exception exp)
            {
            }
        }
    }

    /**
     * 将对象置为无效并返回对象池（销毁对象）。当被获取（borrow）的对象被认为无效时，调用该方法
     */
    public synchronized void invalidateObject(Object obj)
    {
        if (null == obj)
        {
            return;
        }
        // 从活跃对象池移除
        m_activeObjectPool.remove(obj);
        try
        {
            // 销毁无效对象
            m_objectFactory.destroyObject(obj);
        }
        catch (Exception exp)
        {
        }

    }

    /**
     * 设置用于构造/销毁对象的工厂类
     *
     * @param factory
     */
    public void setFactory(PoolableObjectFactory factory)
    {
        m_objectFactory = factory;
    }

    /**
     * 当前活跃的（被获取的）对象数
     *
     * @return
     */
    public synchronized int getActiveObjectCount()
    {
        return m_activeObjectPool.size();
    }

    /**
     * 当前非活跃的对象数
     *
     * @return
     */
    public synchronized int getIdleObjectCount()
    {
        return m_coreIdleObjectPool.size() + m_extIdleObjectPool.size();
    }

    public synchronized int getNumTotal()
    {
        return m_coreIdleObjectPool.size() + m_extIdleObjectPool.size() + m_activeObjectPool.size();
    }

    /**
     * 资源池最大允许的对象数
     *
     * @return
     */
    public int getMaxPoolSize()
    {
        return m_poolConfig.getMaxSize();
    }

    /**
     * 获取资源池最小保持的对象数
     *
     * @return
     */
    public int getCorePoolSize()
    {
        return m_poolConfig.getCoreSize();
    }

    /**
     * 获取对象工厂
     */
    public PoolableObjectFactory getFactory()
    {
        return m_objectFactory;
    }

    /**
     * 调用工厂对象增加一个对象到资源池中
     */
    private Object addObject() throws Exception
    {
        try
        {
            Object poolableObj = m_objectFactory.makeObject();
            if (m_coreIdleObjectPool.offer(poolableObj))
            {
                return poolableObj;
            }
            else if (m_extIdleObjectPool.offer(poolableObj))
            {
                return poolableObj;
            }
            else
            {
                throw new Exception("offer poolableObject failed");
            }

        }
        catch (Exception exp)
        {
            throw new Exception("addObject failed:" + exp, exp);
        }

    }

    /**
     * 将活跃对象返回对象池
     *
     * @param obj
     */
    private void returnActiveObject(Object obj)
    {
        if (!m_coreIdleObjectPool.offer(obj))
        {
            if (!m_extIdleObjectPool.offer(obj))
            {
                // 返回对象池失败
                try
                {
                    // 销毁返回对象池失败对象
                    m_objectFactory.destroyObject(obj);
                }
                catch (Exception exp)
                {
                }
            }
            else
            {
                // 更新扩展对象最近被使用时间
                m_objectAccessTime.put(obj, Long.valueOf(System.currentTimeMillis()));
            }
        }

    }

    @Override
    public String toString()
    {
        return "[QueueBasedObjectPool. PoolableObject:" + m_objectFactory + "]";
    }

}
/**
 * 对象排序比较器
 *
 * @author HO074172
 *
 * @param <E>
 */
class PoolableObjectComparator<E> implements Comparator<Object>
{
    public int compare(Object o1, Object o2)
    {
        return o1.toString().compareTo(o2.toString());
    }
}