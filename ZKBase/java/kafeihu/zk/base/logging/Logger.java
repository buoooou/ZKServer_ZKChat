package kafeihu.zk.base.logging;

/**
 * 日志工具类。该类可分别写debug、info、warn、error日志。<br>
 * Created by zhangkuo on 2016/11/21.
 */
public class Logger {
    /**
     * 控制台日志工具类
     */

    private static Logger ConsoleLogger = new Logger(new ConsoleLog(), LogLevel.DEBUG);

    /**
     * 日志级别
     */
//    private volatile LoggerType m_logType = LoggerType.LOG4J;
    /**
     * 日志级别
     */
    private volatile LogLevel m_logLevel = LogLevel.WARN;
    /**
     * Debug日志对象
     */
    private volatile ILog m_debugLog;
    /**
     * Info日志对象
     */
    private volatile ILog m_infoLog;
    /**
     * Warn日志对象
     */
    private volatile ILog m_warnLog;
    /**
     * Error日志对象
     */
    private volatile ILog m_errorLog;

    /**
     * 构造函数
     *
     * @param log      日志类。
     * @param logLevel 日志级别
     */
    public Logger(ILog log, LogLevel logLevel) {
        m_errorLog = log;
        m_warnLog = log;
        m_infoLog = log;
        m_debugLog = log;
        m_logLevel = logLevel;
//        m_logType = LoggerType.LOG4J;
    }

    /**
     * 构造函数
     *
     * @param errorLog 错误信息日志类
     * @param warnLog  警告信息日志类
     * @param infoLog  一般信息日志类
     * @param debugLog 调试信息日志类
     * @param logLevel 日志级别
     */
    public Logger(ILog errorLog, ILog warnLog, ILog infoLog, ILog debugLog, LogLevel logLevel) {
        m_errorLog = errorLog;
        m_warnLog = warnLog;
        m_infoLog = infoLog;
        m_debugLog = debugLog;
        m_logLevel = logLevel;
//        m_logType = loggerType;
    }

    /**
     * 设置日志级别
     *
     * @param mLogLevel
     */
    public void setLogLevel(LogLevel mLogLevel) {
        m_logLevel = mLogLevel;
    }

    /**
     * Debug信息写日志
     *
     * @param tips
     * @param obj
     */
    public void debug(String tips, Object obj) {
        debug(tips, obj.toString());
    }

    /**
     * Debug信息写日志
     *
     * @param tips
     * @param text
     */
    public void debug(String tips, String text) {

        if (m_logLevel.compareTo(LogLevel.DEBUG) <= 0) {
            m_debugLog.writeLog("[DEBGU] " + tips, text);
        }
    }

    /**
     * Info信息写日志
     *
     * @param tips
     * @param obj
     */
    public void info(String tips, Object obj) {
        info(tips, obj.toString());

    }

    /**
     * Info信息写日志
     *
     * @param tips
     * @param text
     */
    public void info(String tips, String text) {
        if (m_logLevel.compareTo(LogLevel.INFO) <= 0) {
            m_infoLog.writeLog("[INFO] " + tips, text);
        }
    }

    /**
     * Warn信息写日志
     *
     * @param tips
     * @param obj
     */
    public void warn(String tips, Object obj) {
        warn(tips, obj.toString());
    }

    /**
     * Warn信息写日志
     *
     * @param tips
     * @param obj
     */
    public void warn(String tips, String text) {
        if (m_logLevel.compareTo(LogLevel.WARN) <= 0) {
            m_warnLog.writeLog("[WARN] " + tips, text);
        }
    }

    /**
     * Error信息写日志
     *
     * @param tips
     * @param obj
     */
    public void error(String tips, Object obj) {
        error(tips, obj.toString());
    }

    /**
     * Error信息写日志
     *
     * @param tips
     * @param text
     */
    public void error(String tips, String text) {
        if (m_logLevel.compareTo(LogLevel.ERROR) <= 0) {
            m_errorLog.writeLog("[ERROR] " + tips, text);
        }
    }

    /**
     * 将缓冲日志内容写入文件
     */
    public void flush() {
        flushDebug();
        flushInfo();
        flushWarn();
        flushError();
    }

    /**
     * 关闭日志对象
     */
    public void close() {
        m_debugLog.closeLog();
        m_infoLog.closeLog();
        m_warnLog.closeLog();
        m_errorLog.closeLog();
    }

    /**
     * 将Debug缓冲日志内容写入文件
     */
    public void flushDebug() {
        m_debugLog.flushLog();
    }

    /**
     * 将Info缓冲日志内容写入文件
     */
    public void flushInfo() {
        m_infoLog.flushLog();
    }

    /**
     * 将Warn缓冲日志内容写入文件
     */
    public void flushWarn() {
        m_warnLog.flushLog();
    }

    /**
     * 将Error缓冲日志内容写入文件
     */
    public void flushError() {
        m_errorLog.flushLog();
    }

    public ILog getDebugLog() {
        return m_debugLog;
    }

    public ILog getInfoLog() {
        return m_infoLog;
    }

    public ILog getWarnLog() {
        return m_warnLog;
    }

    public ILog getErrorLog() {
        return m_errorLog;
    }

    /**
     * 获取控制台日志工具类：将日志输出到控制台，日志级别为DEBUG
     *
     * @return
     */
    public static Logger getConsoleLogger() {
        return ConsoleLogger;
    }
}
