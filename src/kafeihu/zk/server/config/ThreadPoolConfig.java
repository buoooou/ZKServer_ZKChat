package kafeihu.zk.server.config;

import kafeihu.zk.base.util.XmlUtil;

/**
 * 线程池配置类
 * Created by zhangkuo on 2016/11/25.
 */
public class ThreadPoolConfig {
    /**
     * 池中允许的最大线程数
     */
    private int maxPoolSize;
    /**
     * 池中所保存的线程数，包括空闲线程
     */
    private int corePoolSize;
    /**
     * 执行前用于保持任务的队列大小。队列仅保持由 execute 方法提交的 Runnable 任务
     */
    private int workQueueSize;
    /**
     * 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间
     */
    private long keepAliveTimeSeconds;
    /**
     * 由于超出线程范围和队列容量而使执行被阻塞时所使用的处理程序类名
     */
    private String rejectedExecutionHandler;

    public int getWorkQueueSize()
    {
        return workQueueSize;
    }

    public void setWorkQueueSize(int workQueueSize)
    {
        this.workQueueSize = workQueueSize;
    }

    public int getMaxPoolSize()
    {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize)
    {
        this.maxPoolSize = maxPoolSize;
    }

    public int getCorePoolSize()
    {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize)
    {
        this.corePoolSize = corePoolSize;
    }

    public String getRejectedExecutionHandler()
    {
        return rejectedExecutionHandler;
    }

    public void setRejectedExecutionHandler(String rejectedExecutionHandler)
    {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    public long getKeepAliveTimeSeconds()
    {
        return keepAliveTimeSeconds;
    }

    public void setKeepAliveTimeSeconds(long keepAliveTimeSeconds)
    {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

    /**
     * 根据XML配置数据，解析线程池配置类
     *
     * @param xmlConfig
     * @return
     * @throws Exception
     */
    public static ThreadPoolConfig parseThreadPoolConfig(String xmlConfig) throws Exception
    {
        ThreadPoolConfig config = new ThreadPoolConfig();
        String tmpValue = "";
        // 解析核心线程数
        int corePoolSize;
        try
        {
            tmpValue = XmlUtil.getXmlElement("corePoolSize", xmlConfig);
            corePoolSize = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal corePoolSize value:" + tmpValue);
        }
        config.setCorePoolSize(corePoolSize);
        // 解析最大线程数
        int maxPoolSize;
        try
        {
            tmpValue = XmlUtil.getXmlElement("maxPoolSize", xmlConfig);
            maxPoolSize = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal maxPoolSize value:" + tmpValue);
        }
        config.setMaxPoolSize(maxPoolSize);
        // 解析排队队列
        int workQueueSize;
        try
        {
            tmpValue = XmlUtil.getXmlElement("workQueueSize", xmlConfig);
            workQueueSize = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal workQueueSize value:" + tmpValue);
        }
        config.setWorkQueueSize(workQueueSize);
        // 解析多余线程存活时间
        long keepAliveTimeSeconds;
        try
        {
            tmpValue = XmlUtil.getXmlElement("keepAliveTimeSeconds", xmlConfig);
            keepAliveTimeSeconds = Long.parseLong(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal keepAliveTimeSeconds value:" + tmpValue);
        }
        config.setKeepAliveTimeSeconds(keepAliveTimeSeconds);
        // 解析执行拒绝处理器
        String rejectedExecutionHandler = XmlUtil.getXmlElement("rejectedExecutionHandler",
                xmlConfig);
        config.setRejectedExecutionHandler(rejectedExecutionHandler);
        return config;
    }
}
