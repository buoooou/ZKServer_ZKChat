package kafeihu.zk.bserver.manager;

import kafeihu.zk.base.util.ResourceUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public final class Log4JManager {


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
    public static LoggerUtil getSysLogger() {

        return new LoggerUtil();
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
        //m_debugLogger.debug("dsd");getSysLogger()
        getSysLogger().info("dafafasddff");
    }
}
class LoggerUtil{

    /**
     * 系统日志处理器实例
     */
    private static Logger m_debugLogger = Logger.getLogger("DEBUGLOG");
    private static Logger m_infoLogger = Logger.getLogger("INFOLOG");
    private static Logger m_warnLogger = Logger.getLogger("WARNLOG");
    private static Logger m_errorLogger = Logger.getLogger("ERRORLOG");

    public static void info(String log){
        m_infoLogger.info(log);
    };

    public static void warn(String log){
        m_warnLogger.warn(log);
    };


    public static void error(String log){
        m_errorLogger.error(log);
    };

    public static void debug(String log){
        m_debugLogger.debug(log);
    };

}
