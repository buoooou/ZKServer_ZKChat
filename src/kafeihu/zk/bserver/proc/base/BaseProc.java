package kafeihu.zk.bserver.proc.base;

import kafeihu.zk.bserver.config.GlobalConfig;
import kafeihu.zk.bserver.context.ModuleContext;
import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.bserver.manager.ContextManager;
import kafeihu.zk.bserver.manager.LoggerManager;
import kafeihu.zk.bserver.statistics.IStatistics;
import kafeihu.zk.base.util.XmlUtil;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server业务处理基类，子类需继承该类并实现doProc方法
 * Created by zhangkuo on 2016/11/25.
 */
public abstract class BaseProc implements IStatistics {

    /**
     * 模块名称
     */
    private String m_moduleName;
    /**
     * 业务处理类编号
     */
    private String m_procId;
    /**
     * 最大并发数
     */
    private volatile int m_maxActive = 0;
    /**
     * 最大并发数出现时间
     */
    private volatile long m_maxActiveOccurTime = System.currentTimeMillis();
    /**
     * Proc启动时间
     */
    private long m_startUpTime = System.currentTimeMillis();
    /**
     * 当前并发数
     */
    private AtomicInteger m_active = new AtomicInteger(0);
    /**
     * 自应用开始工作以来的成功交易总数
     */
    private AtomicLong m_succ = new AtomicLong(0);
    /**
     * 成功交易总耗时
     */
    private AtomicLong m_totalCostMills = new AtomicLong(0);
    /**
     * 交易处理最大耗时
     */
    private volatile long m_maxCostMills = 0L;
    /**
     * 最大耗时发生时间
     */
    private volatile long m_maxCostOccurTime = System.currentTimeMillis();

    /**
     * 自应用开始工作以来的失败交易总数
     */
    private AtomicLong m_fail = new AtomicLong(0);

    public BaseProc(String mModuleName, String mProcId)
    {
        super();
        m_moduleName = mModuleName;
        m_procId = mProcId;
    }

    private Properties m_propParams;

    /**
     * 当前并发数加一
     *
     */
    private void addActive()
    {
        int activeNum = m_active.incrementAndGet();
        if (m_maxActive < activeNum)
        {
            synchronized (this)
            {
                if (m_maxActive < activeNum)
                {
                    m_maxActive = activeNum;
                    m_maxActiveOccurTime = System.currentTimeMillis();
                    // 写日志
                    StringBuilder sb = new StringBuilder();
                    sb.append("Prid:" + getProcId());
                    sb.append(" MaxActiveCnt:").append(m_maxActive);
                    getModuleLogger().warn(getClass().getName(), sb.toString());
                }
            }
        }
    }

    /**
     * 当前并发数减一
     */
    private void decActive()
    {
        m_active.decrementAndGet();
    }

    /**
     * 增加交易失败数
     *
     */
    private void addFail()
    {
        m_fail.incrementAndGet();
    }

    /**
     * 增加成功数
     *
     */
    private void addSucc(long timeUsingMills)
    {
        m_succ.incrementAndGet();
        m_totalCostMills.addAndGet(timeUsingMills);
        if (m_maxCostMills <= timeUsingMills)
        {
            synchronized (this)
            {
                if (m_maxCostMills <= timeUsingMills)
                {
                    m_maxCostMills = timeUsingMills;
                    m_maxCostOccurTime = System.currentTimeMillis();
                    // 写日志
                    StringBuilder sb = new StringBuilder();
                    sb.append("Prid:" + getProcId());
                    sb.append(" MaxCostMills:").append(m_maxCostMills);
                    getModuleLogger().warn(getClass().getName(), sb.toString());
                }
            }
        }
    }

    /**
     * 业务处理类初始化方法：该方法在业务类实例构造完成并设置好参数后调用。子类可重写该方法来执行特定的初始化处理
     *
     * @throws Exception
     */
    public void init() throws Exception
    {
    }

    /**
     * 业务处理接口。Socket请求处理器调用该接口处理业务
     *
     * @param reqData
     * @return
     * @throws Exception
     */
    public final Object doBaseProc(Object reqData) throws Exception
    {
        addActive();
        try
        {
            long lStart = System.currentTimeMillis();
            Object result = doProc(reqData);
            long lTimeUsing = System.currentTimeMillis() - lStart;
            // 增加成功交易数
            addSucc(lTimeUsing);
            // 对用时超长的交易进行报警
            if (lTimeUsing >= GlobalConfig.timeConsumingWarningMills)
            {
                onProcTimeConsumingWarning(lTimeUsing,
                        GlobalConfig.timeConsumingWarningMills);
            }

            return result;
        }
        catch (Exception exp)
        {
            // 增加失败交易数
            addFail();
            // 异常处理
            onException(exp);
            throw exp;
        }
        finally
        {
            decActive();
        }
    }

    /**
     * 系统发生异常时调用。默认为写错误日志。子类可重写该方法
     *
     * @param exp
     */
    protected void onException(Exception exp)
    {
        getModuleLogger().error(getClass().getName(), "Exception:"+exp.getMessage());
    }

    /**
     * 对于耗时超过阀值的处理，调用该方法预警：默认为写日志。子类可重写该方法
     *
     * @param timeConsumingMills
     *            处理耗时毫秒数
     * @param timeConsumingThresholdMills
     *            预警阀值毫秒数
     */
    protected void onProcTimeConsumingWarning(long timeConsumingMills,
                                              long timeConsumingThresholdMills)
    {
        getModuleLogger().warn(getClass().getName(),
                "TimecostMills:" + timeConsumingMills);
    }

    /**
     * 业务处理接口。子类需实现该接口完成真正的业务处理
     *
     * @param reqData
     * @return
     * @throws Exception
     */
    protected abstract Object doProc(Object reqData) throws Exception;

    /**
     * 获取模块名称
     *
     * @return
     */
    public String getModuleName()
    {
        return m_moduleName;
    }

    /**
     * 获取模块日志处理类
     *
     * @return
     */
    public Logger getModuleLogger()
    {
        return LoggerManager.getModuleLogger(m_moduleName);
    }

    /**
     * 获取指定id的数据库连接池
     *
     * @param id
     * @return
     * @throws Exception
     */
//	public DBConnectionPool getModuleDBConnectionPool(String id)
//			throws Exception
//	{
//		return DBConnectionPoolManager.getModuleDBConnectionPool(m_moduleName,
//				id);
//	}

    /**
     * 获取模块上下文
     *
     * @return
     */
    public ModuleContext getModuleContext() throws Exception
    {
        return ContextManager.getModuleContext(m_moduleName);
    }

    /**
     * 最大并发请求数
     *
     * @return
     */
    public int getMaxActiveCount()
    {
        return m_maxActive;
    }

    /**
     * 最大并发请求数发生时间
     *
     * @return
     */
    public long getMaxActiveCountOccurTime()
    {
        return m_maxActiveOccurTime;
    }

    /**
     * 当前并发请求数
     *
     * @return
     */
    public int getActiveCount()
    {
        return m_active.get();
    }

    /**
     * 处理成功交易数
     *
     * @return
     */
    public long getSuccCount()
    {
        return m_succ.get();
    }

    /**
     * （成功）交易处理总耗时（毫秒）
     * @return
     */
    public long getTotalCostMills()
    {
        return m_totalCostMills.get();
    }
    /**
     * 交易处理最大耗时（毫秒）
     *
     * @return
     */
    public long getMaxCostMills()
    {
        return m_maxCostMills;
    }

    /**
     * 交易处理最大耗时发生时间
     *
     * @return
     */
    public long getMaxCostOccurTime()
    {
        return m_maxCostOccurTime;
    }

    /**
     * 处理失败交易数
     *
     * @return
     */
    public long getFailCount()
    {
        return m_fail.get();
    }

    /**
     * 获取业务处理ID
     *
     * @return
     */
    public String getProcId()
    {
        return m_procId;
    }

    public void setParamData(String paramData)
    {
        m_propParams = XmlUtil.parseProperties(paramData);
    }

    public String getParam(String paramName)
    {
        return getParam(paramName, "");
    }

    public String getParam(String paramName, String strDefaultValue)
    {
        return m_propParams.getProperty(paramName, strDefaultValue);
    }

    public Object getStatistics()
    {

        StringBuilder sbStatistics = new StringBuilder();
        sbStatistics.append("<statistics>");
        sbStatistics.append("<type>").append(IStatistics.StatType_Proc)
                .append("</type>");
        // 模块名
        sbStatistics.append("<mod>").append(m_moduleName).append("</mod>");
        // 业务ID
        sbStatistics.append("<id>").append(m_procId).append("</id>");
        // 当前并发请求数
        sbStatistics.append("<act>").append(m_active.get()).append("</act>");
        // 最大并发请求数
        sbStatistics.append("<max>").append(m_maxActive).append("</max>");
        // 自服务启动以来已成功处理的客户请求数
        sbStatistics.append("<sn>").append(m_succ.get()).append("</sn>");
        // 处理请求总耗时
        sbStatistics.append("<tcm>").append(m_totalCostMills.get())
                .append("</tcm>");
        // 最大处理耗时
        sbStatistics.append("<mcm>").append(m_maxCostMills).append("</mcm>");
        // 最大处理耗时发生时间
        sbStatistics.append("<mct>").append(m_maxCostOccurTime)
                .append("</mct>");
        // 自服务启动以来处理失败的客户请求数
        sbStatistics.append("<fn>").append(m_fail.get()).append("</fn>");
        // 服务启动时间
        sbStatistics.append("<sut>").append(m_startUpTime).append("</sut>");
        sbStatistics.append("</statistics>");

        return sbStatistics.toString();
    }
}

