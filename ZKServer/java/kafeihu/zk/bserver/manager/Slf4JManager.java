package kafeihu.zk.bserver.manager;

import kafeihu.zk.base.util.ResourceUtil;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhangkuo on 2017/8/25.
 */
public class Slf4JManager {

    private static Logger m_monitorLogger = LoggerFactory.getLogger("monitor");
    private static Logger m_zkchatLogger = LoggerFactory.getLogger("zkchat");
    private static Logger m_sysLogger = LoggerFactory.getLogger("system");
    private static Logger m_consoleLogger = LoggerFactory.getLogger("console");

    static {
        try {
            String path = ResourceUtil.getSysDataPath();
            PropertyConfigurator.configure(path + "log4j.properties");
        } catch (Exception exp) {
            throw new ExceptionInInitializerError(Slf4JManager.class.getName()
                    + ".initialize().  " + exp);
        }
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
    public static Logger getZKChatLogger() {
        return m_zkchatLogger;
    }

    /**
     * 获取指定模块关联的日志处理器
     *
     * @return
     */
    public static Logger getMonitorLogger() {
        return m_monitorLogger;
    }

    /**
     * 获取控制器模块
     *
     * @return
     */
    public static Logger getConsoleLogger() {
        return m_consoleLogger;
    }
}
