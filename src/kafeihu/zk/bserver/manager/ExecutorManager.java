package kafeihu.zk.bserver.manager;

import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * 异步调度器
 *
 * Created by zhangkuo on 2017/6/11.
 */
public class ExecutorManager {
    /**
     * 配置文件
     */
    private final static String Config_File_Name = "executor-config.xml";
    /**
     * ScheduledThreadPoolExecutor实例容器
     */
    private final static Map<String, ScheduledThreadPoolExecutor> m_ScheduledThreadPoolExecutorInstMap = new ConcurrentHashMap<String, ScheduledThreadPoolExecutor>(
            20, 0.8f, 1);
    private static ScheduledThreadPoolExecutor m_defaultScheduledThreadPoolExecutorInst = new ScheduledThreadPoolExecutor(
            5);

    static
    {
        try
        {
            System.out.print("Initializing ExecutorManager...... ");
            initialize();
            System.out.println("OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(
                    ExecutorManager.class.getName() + ".initialize(). " + exp);
        }
    }

    private ExecutorManager()
    {
        // TODO Auto-generated constructor stub
    }

    private static void initialize() throws Exception
    {
        if (ResourceUtil.isSysDataResourceExists(Config_File_Name))
        {
            String configData = ResourceUtil
                    .getSysDataResourceContent(Config_File_Name);
            try
            {
                parseScheduledThreadPoolExecutorCfg(configData);
            }
            catch (Exception exp)
            {
                throw new Exception(
                        "initialize ScheduledThreadPoolExecutor failed. exp:"
                                + exp.getMessage());
            }

        }
    }

    private static void parseScheduledThreadPoolExecutorCfg(String xmlConfigData)
            throws Exception
    {
        // 解析并构造数据库连接池实例
        List<String> listExecutorConfig = XmlUtil.getAllXmlElements(
                "scheduledThreadPoolExecutor", xmlConfigData);

        for (String xmlExecutorConfig : listExecutorConfig)
        {
            String id = XmlUtil.getXmlElement("id", xmlExecutorConfig);
            int poolSize = MiscUtil.parseInt(
                    XmlUtil.getXmlElement("poolSize", xmlExecutorConfig), 2);
            if (poolSize <= 0)
            {
                throw new Exception("illegal poolSize : " + poolSize);
            }
            if (MiscUtil.isEmpty(id))
            {
                m_defaultScheduledThreadPoolExecutorInst = new ScheduledThreadPoolExecutor(
                        poolSize);
            }
            else
            {
                if (m_ScheduledThreadPoolExecutorInstMap.containsKey(id))
                {
                    throw new Exception(
                            " duplicate scheduledThreadPoolExecutor defined. id:"
                                    + id);
                }

                ScheduledThreadPoolExecutor executorInst = new ScheduledThreadPoolExecutor(
                        poolSize);
                m_ScheduledThreadPoolExecutorInstMap.put(id, executorInst);
            }
        }
    }

    /**
     * 根据指定ID获取调度线程池
     *
     * @param id
     * @return
     */
    public static ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor(
            String id)
    {
        ScheduledThreadPoolExecutor executor = m_ScheduledThreadPoolExecutorInstMap
                .get(id);
        if (null == executor)
        {
            return m_defaultScheduledThreadPoolExecutorInst;
        }
        return executor;
    }

    public static void stopExecutor()
    {
        try
        {
            // 按顺序执行已提交的任务，不再接受新的任务

            Collection<ScheduledThreadPoolExecutor> scheduledThreadPoolExecutorCol = m_ScheduledThreadPoolExecutorInstMap
                    .values();
            for (ScheduledThreadPoolExecutor scheduledThreadPoolExecutor : scheduledThreadPoolExecutorCol)
            {
                scheduledThreadPoolExecutor.shutdown();
                // 等待所有任务结束
                int cnt = 0;
                while ((!scheduledThreadPoolExecutor.awaitTermination(5,
                        TimeUnit.SECONDS)) && (cnt < 3))
                {
                    cnt++;
                }
            }
        }
        catch (InterruptedException exp)
        {
        }
    }

}
