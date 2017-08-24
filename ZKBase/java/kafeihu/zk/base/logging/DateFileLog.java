package kafeihu.zk.base.logging;

import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public class DateFileLog implements ILog {
    /**
     * 日志类型：以天为单位写日志文件
     */
    public final static int SPAN_TYPE_DAY = 1;
    /**
     * 日志类型：以月为单位写日志文件
     */
    public final static int SPAN_TYPE_MONTH = 2;
    /**
     * 日志类型：以年为单位写日志文件
     */
    public final static int SPAN_TYPE_YEAR = 3;

    /**
     * 日志类型
     */
    private int m_logSpanType; // Log file type(1, 2, 3)
    /**
     * 日志文件路径
     */
    private String m_logFilePath; // Log file path
    /**
     * 日志文件名头字符串
     */
    private String m_logFileNamePrefix; // Log file name's head string
    /**
     * 日志文件名后缀
     */
    private String m_logFileNameSuffix = ".log";
    /**
     * 写日志操作失败标志
     */
    private boolean m_error;// Log file operate error flag
    /**
     * 当前日志时间
     */
    private String m_logDate = "";
    /**
     * 当前日志文件输出流
     */
    private BufferedOutputStream m_bosLog = null;
    /**
     * 是否使用缓存
     */
    private boolean m_usingBuffer = true;

    private SimpleDateFormat m_dateFromat;

    /**
     * 缓冲区大小（字节数）
     */
    private int m_bufferSize;

    private String m_encoding = System.getProperty("file.encoding");
    private final String m_lineSep = System.getProperty("line.separator");

    /**
     * 构造函数。日志文件后缀.log，写日志时使用缓冲区，缓冲区大小10000
     *
     * @param path
     *            日志文件保存路径
     * @param fnamePrefix
     *            日志文件名前缀
     * @param spanType
     *            日志文件类型（年、月、日）。根据类型构造日志文件名
     *
     * @throws Exception
     */
    public DateFileLog(String path, String fnamePrefix, int spanType) throws Exception
    {
        this(path, fnamePrefix, spanType, true);
    }

    /**
     * 构造函数。日志文件后缀.log，缓冲区大小10000
     *
     * @param path
     *            日志文件保存路径
     * @param fnamePrefix
     *            日志文件名前缀
     * @param spanType
     *            日志文件类型（年、月、日）。根据类型构造日志文件名
     * @param usingBuffer
     *            写日志时是否使用缓冲区
     *
     * @throws Exception
     */
    public DateFileLog(String path, String fnamePrefix, int spanType, boolean usingBuffer)
            throws Exception
    {
        this(path, fnamePrefix, ".log", spanType, usingBuffer, 10000);
    }

    /**
     * 构造函数。缓冲区大小10000
     *
     * @param path
     *            日志文件保存路径
     * @param fnamePrefix
     *            日志文件名前缀
     * @param fnameSuffix
     *            日志文件名后缀，如：.log,.txt等
     * @param spanType
     *            日志文件类型（年、月、日）。根据类型构造日志文件名
     * @param usingBuffer
     *            写日志时是否使用缓冲区
     *
     * @throws Exception
     */
    public DateFileLog(String path, String fnamePrefix, String fnameSuffix, int spanType,
                       boolean usingBuffer) throws Exception
    {
        this(path, fnamePrefix, fnameSuffix, spanType, usingBuffer, 10000);
    }

    /**
     * 构造函数。
     *
     * @param path
     *            日志文件保存路径
     * @param fnamePrefix
     *            日志文件名前缀
     * @param fnameSuffix
     *            日志文件名后缀，如：.log,.txt等
     * @param spanType
     *            日志文件类型（年、月、日）。根据类型构造日志文件名
     * @param usingBuffer
     *            写日志时是否使用缓冲区
     * @param bufferSize
     *            缓冲区大小
     * @throws Exception
     */
    public DateFileLog(String path, String fnamePrefix, String fnameSuffix, int spanType,
                       boolean usingBuffer, int bufferSize) throws Exception
    {
        // 处理日志存放路径
        m_logFilePath = path;
        File tmpFile = new File(path);
        if (!tmpFile.exists())
        {
            if (!tmpFile.mkdirs())
            {
                throw new Exception("create directory for log file failed. path=" + path);
            }
        }

        if (!tmpFile.isDirectory())
        {
            throw new Exception("illegal path for log file. path=" + path);
        }
        if (fnamePrefix.trim().length() <= 0)
        {
            throw new Exception("illegal prefix for log file : can not be empty ");
        }
        m_logFileNamePrefix = fnamePrefix.trim();
        if (!fnameSuffix.trim().startsWith("."))
        {
            throw new Exception("illegal suffix for log file : must start with '.' ");
        }
        m_logFileNameSuffix = fnameSuffix.trim();

        m_logSpanType = spanType;
        m_usingBuffer = usingBuffer;
        m_bufferSize = bufferSize;
        String strFormat = "yyyyMMdd";
        switch (m_logSpanType)
        {
            case SPAN_TYPE_DAY:
                strFormat = "yyyyMMdd";
                break;
            case SPAN_TYPE_MONTH:
                strFormat = "yyyyMM";
                break;
            case SPAN_TYPE_YEAR:
                strFormat = "yyyy";
                break;
        }
        m_dateFromat = new SimpleDateFormat(strFormat);

        // 打开日志文件
        openLogFile();
    }

    /**
     * 设置写日志时是否开启缓冲
     *
     * @param mUsingBuffer
     */
    public void setUsingBuffer(boolean mUsingBuffer)
    {
        m_usingBuffer = mUsingBuffer;
    }

    /**
     * 返回写日志缓冲标志
     *
     * @return
     */
    public boolean isUsingBuffer()
    {
        return m_usingBuffer;
    }

    /**
     * 返回缓冲区大小
     *
     * @return
     */
    public int getBufferSize()
    {
        return m_bufferSize;
    }

    /**
     * 设置缓冲区大小
     *
     * @param mBufferSize
     */
    public void setBufferSize(int mBufferSize)
    {
        if (mBufferSize > 0)
        {
            m_bufferSize = mBufferSize;
        }
    }

    /**
     * 返回日志跨度类型：天、月、年
     *
     * @return
     */
    public int getLogSpanType()
    {
        return m_logSpanType;
    }

    /**
     * 返回日志文件存放路径
     *
     * @return
     */
    public String getLogFilePath()
    {
        return m_logFilePath;
    }

    /**
     * 返回日志文件名前缀
     *
     * @return
     */
    public String getLogFileNamePrefix()
    {
        return m_logFileNamePrefix;
    }

    /**
     * 返回日志文件名后缀
     *
     * @return
     */
    public String getLogFileNameSuffix()
    {
        return m_logFileNameSuffix;
    }

    /**
     * 设置日志内容编码格式。默认为系统默认编码
     *
     * @param mEncoding
     */
    public synchronized void setEncoding(String mEncoding)
    {
        try
        {
            "s".getBytes(mEncoding);
            m_encoding = mEncoding;
        }
        catch (Exception exp)
        {
        }
    }

    /**
     * 根据日期，构造日志文件名
     * @param logDate
     * @return
     */
    protected String createLogFileName(String logDate)
    {
        return getLogFilePath() + File.separator + getLogFileNamePrefix() + "-" + logDate
                + getLogFileNameSuffix();
    }

    /**
     * 根据日志日期，构造新的日志文件输出流
     *
     * @param newLogDate
     * @param oldLogDate
     * @return
     * @throws IOException
     */
    protected BufferedOutputStream createNewLogFile(String newLogDate, String oldLogDate)
            throws IOException
    {
        String logFileName = createLogFileName(newLogDate);
        OutputStream osLog = new FileOutputStream(logFileName, true);
        return new BufferedOutputStream(osLog, getBufferSize());
    }

    /**
     * 打开日志文件：如果日志关联日期发生变化，构造新日志文件
     *
     * @throws IOException
     */
    private void openLogFile() throws IOException
    {
        // 判断日志日期是否变化
        String currentLogDate = m_dateFromat.format(new Date());
        if (m_logDate.equals(currentLogDate))
        {
            return;
        }
        // 关闭旧日志输出流
        closeLog();
        // 构造新的日志输出流
        m_bosLog = createNewLogFile(currentLogDate, m_logDate);
        // 设置日志时间
        m_logDate = currentLogDate;
        m_error = false;
    }

    /**
     * 强制将缓冲区中的内容写入文件
     */
    public synchronized void flushLog()
    {
        try
        {
            if (m_bosLog != null)
            {
                m_bosLog.flush();
            }
        }
        catch (Exception e)
        {
        }
    }

    /**
     * 关闭日志输出流
     */
    public synchronized void closeLog()
    {
        try
        {
            flushLog();
            if (m_bosLog != null)
            {
                m_bosLog.close();
            }
        }
        catch (Exception e)
        {
        }

        m_error = true;
    }

    /**
     * 写日志
     *
     * @param tips
     *            日志信息提示
     * @param text
     *            日志信息内容
     */
    public synchronized void writeLog(String tips, String text)
    {
        try
        {
            openLogFile();
            StringBuilder sb = new StringBuilder(MiscUtil.getTimestamp());
            sb.append("  ").append(tips).append("  ").append(text);
            if (m_error)
            {
                System.err.println(sb.toString());
                return;
            }
            sb.append(m_lineSep);
            m_bosLog.write(sb.toString().getBytes(m_encoding));
            if (!m_usingBuffer)
            {
                flushLog();
            }
        }
        catch (IOException e)
        {
            System.err.println(getClass().getName()+".writeLog() failed. exp:"+e.getMessage());
            m_error = true;
        }
    }

    /**
     * 根据给定的日期（格式：yyyyMMdd），获取相应的全路径日志文件名
     *
     * @param logDate
     * @return
     */
    public String getLogFileName(String logDate)
    {
        String logNow = getLogSpanDate(logDate);
        String logFileName = createLogFileName(logNow);
        return logFileName;
    }

    /**
     * 根据日志类型返回相关日期
     * @param logYYYYMMDDDate
     * @return
     */
    protected String getLogSpanDate(String logYYYYMMDDDate)
    {
        String logSpanDate = logYYYYMMDDDate;
        try
        {
            switch (m_logSpanType)
            {
                case SPAN_TYPE_DAY:
                    logSpanDate = logYYYYMMDDDate;
                    break;
                case SPAN_TYPE_MONTH:
                    logSpanDate = logYYYYMMDDDate.substring(0, 6);
                    break;
                case SPAN_TYPE_YEAR:
                    logSpanDate = logYYYYMMDDDate.substring(0, 4);
                    break;
            }
        }
        catch (Throwable t)
        {
        }
        return logSpanDate;
    }
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_logFileNamePrefix == null) ? 0 : m_logFileNamePrefix.hashCode());
        result = prime * result
                + ((m_logFileNameSuffix == null) ? 0 : m_logFileNameSuffix.hashCode());
        result = prime * result + ((m_logFilePath == null) ? 0 : m_logFilePath.hashCode());
        result = prime * result + m_logSpanType;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DateFileLog other = (DateFileLog) obj;
        if (m_logFileNamePrefix == null)
        {
            if (other.m_logFileNamePrefix != null)
                return false;
        }
        else if (!m_logFileNamePrefix.equals(other.m_logFileNamePrefix))
            return false;
        if (m_logFileNameSuffix == null)
        {
            if (other.m_logFileNameSuffix != null)
                return false;
        }
        else if (!m_logFileNameSuffix.equals(other.m_logFileNameSuffix))
            return false;
        if (m_logFilePath == null)
        {
            if (other.m_logFilePath != null)
                return false;
        }
        else if (!m_logFilePath.equals(other.m_logFilePath))
            return false;
        if (m_logSpanType != other.m_logSpanType)
            return false;
        return true;
    }

    /**
     * 根据xml格式的配置数据，构造日志实例
     * @param xmlConfig
     * @return
     * @throws Exception
     */
    public static ILog buildLog(String xmlConfig) throws Exception
    {
        String path = XmlUtil.getXmlElement("path", xmlConfig);
        if (MiscUtil.isEmpty(path))
        {
            throw new Exception("path can not be empty");
        }

        String prefix = XmlUtil.getXmlElement("prefix", xmlConfig);
        if (MiscUtil.isEmpty(prefix))
        {
            throw new Exception("prefix can not be empty");
        }
        String suffix = XmlUtil.getXmlElement("suffix", xmlConfig,".log");
        if (MiscUtil.isEmpty(suffix))
        {
            throw new Exception("suffix can not be empty");
        }
        if (!suffix.startsWith("."))
        {
            throw new Exception("suffix must start with '.' ");
        }

        int spanType = DateFileLog.SPAN_TYPE_MONTH;
        String sSpanType = XmlUtil.getXmlElement("spanType", xmlConfig, "Month");
        if (sSpanType.equalsIgnoreCase("Day"))
        {
            spanType = DateFileLog.SPAN_TYPE_DAY;
        }
        else if (sSpanType.equalsIgnoreCase("Year"))
        {
            spanType = DateFileLog.SPAN_TYPE_YEAR;
        }

        boolean usingBuffer = XmlUtil.getXmlElement("usingBuffer", xmlConfig, "Y")
                .equalsIgnoreCase("Y");
        int bufferSize = Integer.parseInt(XmlUtil.getXmlElement("bufferSize", xmlConfig,
                "10000"));

        DateFileLog logObj = new DateFileLog(path, prefix, suffix, spanType, usingBuffer, bufferSize);
        //设置日志文字编码格式
        String encoding = XmlUtil.getXmlElement("encoding", xmlConfig);
        if(!MiscUtil.isEmpty(encoding))
        {
            logObj.setEncoding(encoding);
        }
        return logObj;
    }
}
