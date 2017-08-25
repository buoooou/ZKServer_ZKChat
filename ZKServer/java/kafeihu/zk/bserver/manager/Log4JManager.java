package kafeihu.zk.bserver.manager;

import kafeihu.zk.base.util.ResourceUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public final class Log4JManager {

    private static Logger m_monitorLogger = Logger.getLogger("MONITORLOG");
    private static Logger m_zkchatLogger = Logger.getLogger("ZKCHATLOG");
    private static Logger m_sysLogger = Logger.getLogger("SYSLOG");
    private static Logger m_consoleLogger = Logger.getLogger("CONSOLE");

    static
    {
        try
        {
            //Log4JManager.getConsoleLogger().info("Initializing Log4JManager......");
            initialize();
           // Log4JManager.getConsoleLogger().info("Log4JManager OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(Log4JManager.class.getName()
                    + ".initialize().  " + exp);
        }
    }

    private static void initialize() {

        String path= ResourceUtil.getSysDataPath();
        PropertyConfigurator.configure(path+"log4j.properties");

    }

    /**
     * 获取系统日志处理器
     *
     * @return
     */
    public static Logger getSysLogger() {

        return m_sysLogger;
    }
    /**
     * 获取指定模块关联的日志处理器
     *
     * @return
     */
    public static Logger getZKChatLogger()
    {
       return m_zkchatLogger;
    }
    /**
     * 获取指定模块关联的日志处理器
     *
     * @return
     */
    public static Logger getMonitorLogger()
    {
        return m_monitorLogger;
    }
    /**
     * 获取指定模块关联的日志处理器
     *
     * @return
     */
    public static Logger getConsoleLogger()
    {
        return m_consoleLogger;
    }

}

