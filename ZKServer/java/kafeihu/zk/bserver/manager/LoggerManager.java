package kafeihu.zk.bserver.manager;

import kafeihu.zk.base.logging.ILog;
import kafeihu.zk.base.logging.LogLevel;
import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.logging.LoggerType;
import kafeihu.zk.base.logging.log4j.Log4jUtil;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统日志管理类。解析log-config.xml配置文件，设置日志工具类属性
 * <p>
 * Created by zhangkuo on 2016/11/21.
 */
public final class LoggerManager {

    /**
     * 系统日志处理器实例
     */
    private static Logger m_sysLogger;
    private static Map<String, Logger> m_moduleLoggerMap = new ConcurrentHashMap<String, Logger>();
    private final static String Config_File_Name = "log-config.xml";

    private static LoggerType loggerType = null;

    static {
        try {
            Log4jUtil.getConsoleLogger().info("Initializing LoggerManager...... ");
            initialize();
            Log4jUtil.getConsoleLogger().info("Initializing LoggerManager OK!");
        } catch (Exception exp) {
            throw new ExceptionInInitializerError(LoggerManager.class.getName()
                    + ".initialize().  " + exp);
        }
    }

    /**
     * 初始化日志类
     *
     * @throws Exception
     */
    public static void initialize() throws Exception {
        // 系统日志路径
        String sysLogPath = ResourceUtil.getSysLogPath();
        String sysLogConfigData = ResourceUtil.getSysDataResourceContent(Config_File_Name);

        // 初始化系统日志类
        m_sysLogger = initialize(sysLogPath, sysLogConfigData);

        List<String> moduleNameList = ModuleManager.getModuleName();
        for (String moduleName : moduleNameList) {
            // 如果模块内没有日志配置文件，取系统日志配置
            String moduleLogConfigData = sysLogConfigData;
            if (ResourceUtil.isModuleDataResourceExists(moduleName, Config_File_Name)) {
                moduleLogConfigData = ResourceUtil.getModuleDataResourceContent(moduleName,
                        Config_File_Name);
            }
            // 模块日志路径
            String moduleLogPath = ResourceUtil.getModuleLogPath(moduleName);
            // 初始化模块日志类
            Logger moduleLogger = initialize(moduleLogPath, moduleLogConfigData);
            m_moduleLoggerMap.put(moduleName, moduleLogger);

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
     * @param moduleName
     * @return
     */
    public static Logger getModuleLogger(String moduleName) {
        Logger moduleLogger = m_moduleLoggerMap.get(moduleName);
        if (null == moduleLogger) {
            return m_sysLogger;
        } else {
            return moduleLogger;
        }
    }

    /**
     * 解析日志处理器
     *
     * @param logPath
     * @param listXmlConfig
     * @return
     * @throws Exception
     */
    private static Map<String, ILog> parseLogHandler(String logPath, List<String> listXmlConfig)
            throws Exception {
        Map<String, ILog> logHandlers = new HashMap<String, ILog>();

        for (String xmlConfig : listXmlConfig) {
            String id = XmlUtil.getXmlElement("id", xmlConfig);
            if (MiscUtil.isEmpty(id)) {
                throw new Exception("logHandler/id can not be empty");
            }
            if (logHandlers.containsKey(id)) {
                throw new Exception("duplicate logHandler/id defined");
            }
            String path = XmlUtil.getXmlElement("path", xmlConfig);
            if (MiscUtil.isEmpty(path)) {
                xmlConfig = "<path>" + logPath + "</path>" + xmlConfig;
            }
            String logImpl = XmlUtil.getXmlElement("logImpl", xmlConfig,
                    "kafeihu.zk.base.logging.RollDateFileLog");

            try {
                Class<?> logCls = Class.forName(logImpl);
                Method buildMethod = logCls.getMethod("buildLog", String.class);
                ILog log = (ILog) buildMethod.invoke(logCls, xmlConfig);
                if (logHandlers.containsValue(log)) {
                    throw new Exception("duplicate log instance defined");
                }
                logHandlers.put(id, log);
            } catch (InvocationTargetException exp) {
                throw new Exception("buildLog failed:" + exp.getTargetException());
            }
        }
        return logHandlers;
    }

    /**
     * 获取日志处理器
     *
     * @param logHandlers
     * @param xmlConfig
     * @param bindName
     * @return
     * @throws Exception
     */
    private static ILog getLogHandler(Map<String, ILog> logHandlers, String xmlConfig,
                                      String bindName) throws Exception {
        String logId = XmlUtil.getXmlElement(bindName, xmlConfig);
        ILog logHandler = logHandlers.get(logId);
        if (null == logHandler) {
            throw new Exception("no logHandler defined with id=" + logId);
        }
        return logHandler;
    }

    /**
     * 静态初始化接口
     *
     * @param logPath
     * @param xmlConfig
     * @throws Exception
     */
    private static Logger initialize(String logPath, String xmlConfig) throws Exception {
        LogLevel logLevel = LogLevel.WARN;
        // 解析日志记录级别
        String level = XmlUtil.getXmlElement("logLevel", xmlConfig, "Warn");

        if (level.equalsIgnoreCase("Info")) {
            logLevel = LogLevel.INFO;
        } else if (level.equalsIgnoreCase("Debug")) {
            logLevel = LogLevel.DEBUG;
        } else if (level.equalsIgnoreCase("Warn")) {
            logLevel = LogLevel.WARN;
        } else if (level.equalsIgnoreCase("Error")) {
            logLevel = LogLevel.ERROR;
        }
        // 解析日志处理器
        List<String> listLogHandlerConfig = XmlUtil.getAllXmlElements("logHandler", xmlConfig);
        Map<String, ILog> logHandlers = parseLogHandler(logPath, listLogHandlerConfig);

        // 解析并设置日志绑定关系
        String xmlLogBinding = XmlUtil.getXmlElement("logBinding", xmlConfig);

        ILog debugLog = getLogHandler(logHandlers, xmlLogBinding, "debug");
        ILog infoLog = getLogHandler(logHandlers, xmlLogBinding, "info");
        ILog warnLog = getLogHandler(logHandlers, xmlLogBinding, "warn");
        ILog errorLog = getLogHandler(logHandlers, xmlLogBinding, "error");

        return new Logger(errorLog, warnLog, infoLog, debugLog, logLevel);
    }
}
