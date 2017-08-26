//package kafeihu.zk.base.logging.log4j;
//
//import kafeihu.zk.base.util.ResourceUtil;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//
///**
// * Created by zhangkuo on 2017/8/25.
// */
//public final class Log4jUtil {
//
//    private static Logger m_monitorLogger = Logger.getLogger("monitor");
//    private static Logger m_zkchatLogger = Logger.getLogger("zkchat");
//    private static Logger m_sysLogger = Logger.getLogger("system");
//    private static Logger m_consoleLogger = Logger.getLogger("console");
//
//    static {
//        try {
//            String path = ResourceUtil.getSysDataPath();
//            PropertyConfigurator.configure(path + "log4j.properties");
//        } catch (Exception exp) {
//            throw new ExceptionInInitializerError(Log4jUtil.class.getName()
//                    + ".initialize().  " + exp);
//        }
//    }
//
//
//    /**
//     * 获取系统日志处理器
//     *
//     * @return
//     */
//    public static Logger getSysLogger() {
//
//        return m_sysLogger;
//    }
//
//    /**
//     * 获取指定模块关联的日志处理器
//     *
//     * @return
//     */
//    public static Logger getZKChatLogger() {
//        return m_zkchatLogger;
//    }
//
//    /**
//     * 获取指定模块关联的日志处理器
//     *
//     * @return
//     */
//    public static Logger getMonitorLogger() {
//        return m_monitorLogger;
//    }
//
//    /**
//     * 获取控制器模块
//     *
//     * @return
//     */
//    public static Logger getConsoleLogger() {
//        return m_consoleLogger;
//    }
//
//}
