package kafeihu.zk.bserver.manager;

import kafeihu.zk.base.util.ResourceUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public final class Log4JManager {
    /**
     * 系统日志处理器实例
     */
    private static Logger m_debugLogger = Logger.getLogger("DEBUGLOG");
    private static Logger m_infoLogger = Logger.getLogger("INFOLOG");
    private static Logger m_warnLogger = Logger.getLogger("WARNLOG");
    private static Logger m_errorLogger = Logger.getLogger("ERRORLOG");
    private static Logger m_monitorLogger = Logger.getLogger("MONITORLOG");
    private static Logger m_zkchatLogger = Logger.getLogger("ZKCHATLOG");

    static
    {
        try
        {
            System.out.print("Initializing Log4JManager...... ");
            initialize();
            System.out.println("Log4JManager OK!");
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

      //  return m_sysLogger;
        return null;
    }

    /**
     * 获取指定模块关联的日志处理器
     *
     * @param moduleName
     * @return
     */
    public static Logger getModuleLogger(String moduleName)
    {
       return Logger.getLogger(moduleName);
    }

    public static void main(String[] args) {
        m_debugLogger.debug("dsd");
    }
}
