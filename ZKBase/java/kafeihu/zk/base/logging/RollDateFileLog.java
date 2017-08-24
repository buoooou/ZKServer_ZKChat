package kafeihu.zk.base.logging;

import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.io.*;
import java.util.Date;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public class RollDateFileLog extends DateFileLog {

    public RollDateFileLog(String path, String fnamePrefix, int spanType, boolean usingBuffer)
            throws Exception
    {
        super(path, fnamePrefix, spanType, usingBuffer);
        // TODO Auto-generated constructor stub
    }

    public RollDateFileLog(String path, String fnamePrefix, int spanType) throws Exception
    {
        super(path, fnamePrefix, spanType);
        // TODO Auto-generated constructor stub
    }

    public RollDateFileLog(String path, String fnamePrefix, String fnameSuffix, int spanType,
                           boolean usingBuffer, int bufferSize) throws Exception
    {
        super(path, fnamePrefix, fnameSuffix, spanType, usingBuffer, bufferSize);
        // TODO Auto-generated constructor stub
    }

    public RollDateFileLog(String path, String fnamePrefix, String fnameSuffix, int spanType,
                           boolean usingBuffer) throws Exception
    {
        super(path, fnamePrefix, fnameSuffix, spanType, usingBuffer);
        // TODO Auto-generated constructor stub
    }
    protected BufferedOutputStream createNewLogFile(String newLogDate, String oldLogDate)
            throws IOException
    {
        // 当前日志文件名
        String currentLogFileName = getLogFilePath() + File.separator + getLogFileNamePrefix()
                + getLogFileNameSuffix();

        if (!MiscUtil.isEmpty(oldLogDate))
        {
            // 旧的日志文件名
            String oldLogFileName = createLogFileName(oldLogDate);
            // 将当前日志文件重命名为旧日志文件
            File currentLogFile = new File(currentLogFileName);
            if (currentLogFile.exists())
            {
                File oldLogFile = new File(oldLogFileName);
                if (oldLogFile.exists())
                {
                    if(!oldLogFile.delete())
                    {
                        throw new IOException("delete file failed.  filename:"+oldLogFileName);
                    }
                }
                if(!currentLogFile.renameTo(oldLogFile))
                {
                    throw new IOException("rename file failed. oldfilename:"+oldLogFileName+" newfilename:"+currentLogFileName);
                }
            }
        }

        File currentLogFile = new File(currentLogFileName);
        if (currentLogFile.exists())
        {
            String lastAccessDate = getLogSpanDate(MiscUtil.getDate(new Date(currentLogFile.lastModified())));
            String today = getLogSpanDate(MiscUtil.getDate());
            if (!today.equals(lastAccessDate))
            {
                String oldLogFileName = getLogFileName(lastAccessDate);
                File oldLogFile = new File(oldLogFileName);
                if (oldLogFile.exists())
                {
                    if(!oldLogFile.delete())
                    {
                        throw new IOException("delete file failed#1.  filename:"+oldLogFileName);
                    }
                }
                if(!currentLogFile.renameTo(oldLogFile))
                {
                    throw new IOException("rename file failed#1. oldfilename:"+oldLogFileName+" newfilename:"+currentLogFileName);
                }
            }
        }

        // 构造当前日志输出流
        OutputStream osLog = new FileOutputStream(currentLogFileName, true);
        return new BufferedOutputStream(osLog, getBufferSize());
    }

    /**
     * 根据给定的日期（格式：yyyyMMdd），获取相应的全路径日志文件名
     */
    @Override
    public String getLogFileName(String logDate)
    {
        String currentDate = getLogSpanDate(MiscUtil.getDate());
        String logSpanDate = getLogSpanDate(logDate);
        if (logSpanDate.equals(currentDate))
        {
            return getLogFilePath() + File.separator + getLogFileNamePrefix()
                    + getLogFileNameSuffix();
        }
        else
        {
            return super.getLogFileName(logDate);
        }
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

        RollDateFileLog logObj = new RollDateFileLog(path, prefix, suffix, spanType, usingBuffer, bufferSize);
        //设置日志文字编码格式
        String encoding = XmlUtil.getXmlElement("encoding", xmlConfig);
        if(!MiscUtil.isEmpty(encoding))
        {
            logObj.setEncoding(encoding);
        }
        return logObj;
    }
}
