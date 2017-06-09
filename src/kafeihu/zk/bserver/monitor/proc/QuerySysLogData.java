package kafeihu.zk.bserver.monitor.proc;

import kafeihu.zk.base.logging.DateFileLog;
import kafeihu.zk.base.logging.ILog;
import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.util.ArrayUtil;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.SocketKit;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.bserver.manager.LoggerManager;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.Socket;

/**
 * Created by zhangkuo on 2017/6/9.
 */
public class QuerySysLogData extends MonitorProc{

    /**
     * 查询跨度限制。单位：小时。默认为24小时
     */
    private int m_maxQuerySpanHours = 24;

    /**
     * 批量返回数据条数
     */
    private int m_sendBatchNum = 1000;

    //private final static Logger m_logger = LoggerManager.getSysLogger();
    /**
     * 最大返回的日志行数（避免客户端内存溢出）：0表示不限制
     */
    private int m_maxLogDataLineNum = 0;

    @Override
    public void setParams(String xmlParams)
    {
        try
        {
            m_maxQuerySpanHours = Integer.parseInt(XmlUtil.getXmlElement("MaxQuerySpanHours",
                    xmlParams, "24"));
            m_sendBatchNum = Integer.parseInt(XmlUtil.getXmlElement("SendBatchNum", xmlParams,
                    "1000"));
            m_maxLogDataLineNum= Integer.parseInt(XmlUtil.getXmlElement("MaxLogDataLineNum", xmlParams,
                    "0"));
        }
        catch (Exception exp)
        {
        }
    }

    @Override
    public void doProc(Socket socket, String request) throws Exception
    {
        String xmlRequest = (String) request;
        String fromTime = XmlUtil.getXmlElement("fromTime", xmlRequest);
        if (MiscUtil.isEmpty(fromTime))
        {
            throw new Exception("fromTime can not be empty");
        }
        String toTime = XmlUtil.getXmlElement("toTime", xmlRequest);
        if (MiscUtil.isEmpty(toTime))
        {
            throw new Exception("toTime can not be empty");
        }
        if (toTime.compareTo(fromTime) < 0)
        {
            throw new Exception("toTime can not earlier than fromTime");
        }
        String lineSeparator = XmlUtil.getOriginalXmlElement("lineSeparator", xmlRequest, "");
        if (MiscUtil.isEmpty(lineSeparator))
        {
            throw new Exception("lineSeparator can not be empty");
        }
        int spanHours = MiscUtil.getDuringHours(fromTime, toTime);
        if (spanHours > m_maxQuerySpanHours)
        {
            throw new Exception("query span can not exceeds " + m_maxQuerySpanHours + " hours");
        }
        String logType = XmlUtil.getXmlElement("logType", xmlRequest);
        if (MiscUtil.isEmpty(logType))
        {
            throw new Exception("logType can not be empty");
        }
        String module = XmlUtil.getXmlElement("module", xmlRequest);
        if (MiscUtil.isEmpty(module))
        {
            throw new Exception("module can not be empty");
        }

        // 执行日志查询
        doQuery(module,logType, fromTime, toTime, lineSeparator, socket);
    }

    /**
     * 执行日志查询并返回给客户端
     *
     * @param fromTime
     * @param toTime
     * @param lineSpearator
     * @param socket
     * @throws Exception
     */
    private void doQuery(String module,String logType, String fromTime, String toTime, String lineSpearator,
                         Socket socket) throws Exception
    {
        ILog log = getLog(module,logType);
        if (null == log)
        {
            throw new Exception("illegal logType:" + logType+" module:"+module);
        }
        log.flushLog();
        if (!(log instanceof DateFileLog))
        {
            throw new Exception("Log object is not type of " + DateFileLog.class.getName());
        }
        DateFileLog fileLog = (DateFileLog) log;

        SocketKit sockit = new SocketKit(socket);
        String fromDate = fromTime.substring(0, 8);
        String toDate = toTime.substring(0, 8);

        int batchCount = 0;
        int sumCount = 0;
        StringBuilder logDataBuf = new StringBuilder();
        String[] duringDays = MiscUtil.getDuringDays(fromDate, toDate, true, true);

        for (int i = 0; i < duringDays.length; i++)
        {
            String logFileName = fileLog.getLogFileName(duringDays[i]);
            File logFile = new File(logFileName);
            if (logFile.exists())
            {
                FileReader fReader = new FileReader(logFile);
                LineNumberReader lnReader = new LineNumberReader(fReader);
                try
                {
                    String line = null;
                    while ((line = lnReader.readLine()) != null)
                    {
                        if (line.length() < 17)
                        {
                            logDataBuf.append(line).append(lineSpearator);
                            batchCount++;
                            sumCount++;
                        }
                        else
                        {
                            String logTime = line.substring(0, 17);

                            if (logTime.compareTo(fromTime) >= 0)
                            {
                                if (logTime.compareTo(toTime) <= 0)
                                {
                                    logDataBuf.append(line).append(lineSpearator);
                                    batchCount++;
                                    sumCount++;
                                    if (batchCount >= m_sendBatchNum)
                                    {
                                        sendLogData(logDataBuf, sockit, fReader.getEncoding());
                                        MiscUtil.clearStringBuilder(logDataBuf);
                                        batchCount = 0;
                                    }
                                }
                            }
                        }
                        if((m_maxLogDataLineNum>0)&&(m_maxLogDataLineNum < sumCount))
                        {
                            batchCount=0;
                            logDataBuf.append(lineSpearator);
                            logDataBuf.append("============================================");
                            logDataBuf.append(lineSpearator);
                            logDataBuf.append("    overmany log data, reduce query span pls");
                            logDataBuf.append(lineSpearator);
                            logDataBuf.append("============================================");
                            sendLogData(logDataBuf, sockit, fReader.getEncoding());
                            break;
                        }
                    }
                    if (batchCount > 0)
                    {
                        sendLogData(logDataBuf, sockit, fReader.getEncoding());
                        MiscUtil.clearStringBuilder(logDataBuf);
                        batchCount = 0;
                    }
                }
                finally
                {
                    if (null != lnReader)
                    {
                        try
                        {
                            lnReader.close();
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            }
        }
    }

    private ILog getLog(String module,String logType)
    {
        Logger logger = LoggerManager.getModuleLogger(module);
        if (logType.equalsIgnoreCase("debug"))
        {
            return logger.getDebugLog();
        }
        if (logType.equalsIgnoreCase("info"))
        {
            return logger.getInfoLog();
        }
        if (logType.equalsIgnoreCase("warn"))
        {
            return logger.getWarnLog();
        }
        if (logType.equalsIgnoreCase("error"))
        {
            return logger.getErrorLog();
        }
        return null;
    }

    /**
     * 返回日志数据到客户端
     *
     * @param logDataBuf
     * @param sockit
     * @param encoding
     * @throws Exception
     */
    private void sendLogData(StringBuilder logDataBuf, SocketKit sockit, String encoding)
            throws Exception
    {
        String logData = MonitorProc.SuccPrefix + logDataBuf.toString();
        byte[] logDataBytes = logData.getBytes(encoding);
        int len = logDataBytes.length;

        sockit.send(ArrayUtil.joinArray(MiscUtil.htonl(len), logDataBytes));
        // sockit.send(MiscUtil.htonl(len));
        // sockit.send(logDataBytes);
    }

    @Override
    protected String procRequest(String request) throws Exception
    {
        return null;
    }

}
