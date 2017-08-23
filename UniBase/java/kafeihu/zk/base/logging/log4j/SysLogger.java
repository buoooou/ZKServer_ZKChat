package kafeihu.zk.base.logging.log4j;

import org.apache.log4j.Logger;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public class SysLogger {

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
