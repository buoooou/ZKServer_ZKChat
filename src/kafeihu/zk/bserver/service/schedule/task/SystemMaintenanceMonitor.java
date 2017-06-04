package kafeihu.zk.bserver.service.schedule.task;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.schedule.FileChangeWatcher;
import kafeihu.zk.base.schedule.TaskExecutionContext;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.bserver.manager.LoggerManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 系统维护监控器：主要用于配合负载均衡探测。
 * 在特殊情况（系统上线、维护等）下通过设置系统维护标志临时停止系统服务。
 *
 * Created by zhangkuo on 2017/6/3.
 */
public class SystemMaintenanceMonitor extends FileChangeWatcher{

    private final static Logger m_logger = LoggerManager.getSysLogger();

    // 系统维护属性
    private static Properties m_sysMaintainProp = new Properties();

    /**
     * 可重入的读写锁
     */
    private final static ReentrantReadWriteLock m_rwLock = new ReentrantReadWriteLock();

    /**
     * 写锁。独占
     */
    private final static Lock m_writeLock = m_rwLock.writeLock();

    private static String m_flagKey = "SystemMaintenance";

    /**
     * 系统是否处于维护状态
     */
    private static volatile boolean m_isInMaintenance = false;

    private static String m_logTips = SystemMaintenanceMonitor.class.getName();

    public SystemMaintenanceMonitor(String name)
    {
        super(name);
    }

    /**
     * 判断系统是否处于维护状态
     *
     * @return
     */
    public static boolean IsInMaintenance()
    {
        return m_isInMaintenance;

    }

    @Override
    protected File getTargetFile() throws Exception
    {
        String filename = getParam("filename", "systemMaintenance.txt");
        File targetFile = ResourceUtil.getSysDataResourceAsFile(filename);
        // 初始化系统状态
        onFileChanged(targetFile);
        return targetFile;
    }

    @Override
    protected void onFileChanged(File targetFile) throws Exception
    {
        InputStream is = null;
        m_writeLock.lock();
        try
        {
            m_sysMaintainProp.clear();
            is = new FileInputStream(targetFile);
            m_sysMaintainProp.load(is);

            boolean bFlag = m_sysMaintainProp.getProperty(m_flagKey, "N").equalsIgnoreCase("Y");
            if (m_isInMaintenance != bFlag)
            {
                m_isInMaintenance = bFlag;
                m_logger.info(m_logTips, "System is in Maintenance:"
                        + m_sysMaintainProp.getProperty(m_flagKey, "N"));
            }
        }
        finally
        {
            m_writeLock.unlock();
            if (null != is)
            {
                try
                {
                    is.close();
                }
                catch (Exception exp)
                {
                }
            }
        }
    }

    @Override
    public void onExecuteException(TaskExecutionContext context, Exception exp)
    {
        m_logger.error(getClass().getName(), exp.getMessage());
    }
}
