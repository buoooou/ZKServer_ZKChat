package kafeihu.zk.bserver.console;

import kafeihu.zk.base.client.impl.BServerSocketClient;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.bserver.statistics.IStatistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * Created by zhangkuo on 2017/6/9.
 */
public class BServerMonitor {

    private String serverIp = "127.0.0.1";
    private int serverPort = 8010;
    private int loopSeconds = 5;
    private String outputFileName =  "";

    public BServerMonitor(String serverIp, int serverPort, int loopSeconds,
                          String outputFileName)
    {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.loopSeconds = loopSeconds;
        this.outputFileName = outputFileName;
    }

    public void start()
    {
        Runnable worker = new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(loopSeconds * 1000);
                        getStatInfo();
                    }
                    catch (Exception exp)
                    {
                        System.out.println(exp.getMessage());
                    }
                }
            }
        };
        new Thread(worker).start();
        System.out.println("UniBServer monitor started.");
    }

    /**
     * 获取BServer服务器统计信息
     *
     * @throws Exception
     */
    public void getStatInfo() throws Exception
    {
        BServerSocketClient client = new BServerSocketClient(serverIp,
                serverPort, 2000);
        client.setModuleName("monitor");
        client.setProcId("GetStatData");

        byte[] respData = client.sendRecv("getstatdata".getBytes());
        if (respData[0] == '\1')
        {// 处理成功
            String statData = new String(respData).substring(1);

            // 输出统计信息
            // System.out.println(parseNotifyStatInfo(statData));

            // 输出为Html文件
            dump2Html(parseStatInfo2Html(statData));
        }
        else
        {
            System.out.println("GetStatistics Failed! Recv:"
                    + new String(respData).substring(1));
        }
    }

    /**
     * 解析统计数据并构造为Html格式的页面内容
     *
     * @param xmlStatData
     * @return
     */
    private String parseStatInfo2Html(String xmlStatData)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>UniBServer[");
        sb.append(serverIp).append(":").append(serverPort);
        sb.append("]</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body>");
        sb.append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sb.append(
                "<tr bgcolor='#F4F3EE'><td bgcolor='#DFDFDF'>UniBServer</td><td colspan=3>")
                .append(serverIp).append(" : ").append(serverPort)
                .append("</td></tr>");
        DateFormat df = DateFormat.getDateTimeInstance();
        // 服务启动时间
        Date serverStartTime = new Date(Long.parseLong(XmlUtil.getXmlElement(
                "sut", xmlStatData)));
        // 服务器当前时间
        Date serverTime = new Date(Long.parseLong(XmlUtil.getXmlElement("st",
                xmlStatData)));
        sb.append("<tr bgcolor='#F4F3EE'>");
        sb.append("<td bgcolor='#DFDFDF' width=15%>启动时间</td><td>")
                .append(df.format(serverStartTime)).append("</td>");
        sb.append("<td bgcolor='#DFDFDF' width=15%>当前时间</td><td>")
                .append(df.format(serverTime)).append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        StringBuilder sbSocketServer = new StringBuilder();
        sbSocketServer
                .append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sbSocketServer.append("<tr bgcolor='#F4F3EE'>");
        sbSocketServer.append("<td bgcolor='#DFDFDF'>Socket服务</td>");
        sbSocketServer.append("<td bgcolor='#DFDFDF'>端口</td>");
        sbSocketServer.append("<td bgcolor='#DFDFDF'>在线客户数</td>");
        sbSocketServer.append("<td bgcolor='#DFDFDF'>最大并发线程数</td>");
        sbSocketServer.append("<td bgcolor='#DFDFDF'>当前活跃线程数</td>");
        sbSocketServer.append("<td bgcolor='#DFDFDF'>最大允许并发数</td>");
        sbSocketServer.append("</tr>");

        StringBuilder sbProc = new StringBuilder();
        sbProc.append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sbProc.append("<tr bgcolor='#F4F3EE'>");
        sbProc.append("<td bgcolor='#DFDFDF'>模块</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>Prid</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>当前并发数</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>最大并发数</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>成功请求数</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>平均耗时</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>最大耗时</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>最大耗时发生时间</td>");
        sbProc.append("<td bgcolor='#DFDFDF'>失败请求数</td>");
        sbProc.append("</tr>");
        StringBuilder sbObjectPool = new StringBuilder();
        sbObjectPool
                .append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sbObjectPool.append("<tr bgcolor='#F4F3EE'>");
        sbObjectPool.append("<td bgcolor='#DFDFDF'>资源池</td>");
        sbObjectPool.append("<td bgcolor='#DFDFDF'>ID</td>");
        sbObjectPool.append("<td bgcolor='#DFDFDF'>模块</td>");
        sbObjectPool.append("<td bgcolor='#DFDFDF'>当前并发数</td>");
        sbObjectPool.append("<td bgcolor='#DFDFDF'>最大允许并发数</td>");
        sbObjectPool.append("</tr>");

        StringBuilder sbDBConnPool = new StringBuilder();
        sbDBConnPool
                .append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sbDBConnPool.append("<tr bgcolor='#F4F3EE'>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>数据库连接池</td>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>ID</td>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>模块</td>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>活跃连接数</td>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>存活连接数</td>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>空闲连接数</td>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>最小连接数</td>");
        sbDBConnPool.append("<td bgcolor='#DFDFDF'>最大连接数</td>");
        sbDBConnPool.append("</tr>");

        List<String> listxmlStat = XmlUtil.getAllXmlElements("statistics",
                xmlStatData);
        for (String xmlStatItem : listxmlStat)
        {
            String type = XmlUtil.getXmlElement("type", xmlStatItem);
            if (type.equalsIgnoreCase(IStatistics.StatType_SocketServer))
            {
                sbSocketServer.append("<tr bgcolor='#F4F3EE'>");
                // SocketServer别名
                sbSocketServer.append("<td>")
                        .append(XmlUtil.getXmlElement("alias", xmlStatItem))
                        .append("</td>");
                // 监听端口
                sbSocketServer.append("<td>")
                        .append(XmlUtil.getXmlElement("port", xmlStatItem))
                        .append("</td>");
                // 当前在线客户（Socket连接）数
                sbSocketServer.append("<td>")
                        .append(XmlUtil.getXmlElement("ocn", xmlStatItem))
                        .append("</td>");
                // 最大并发线程数
                sbSocketServer.append("<td>")
                        .append(XmlUtil.getXmlElement("max", xmlStatItem))
                        .append("</td>");
                // 当前活跃线程数
                sbSocketServer.append("<td>")
                        .append(XmlUtil.getXmlElement("act", xmlStatItem))
                        .append("</td>");
                // 最大允许并发线程数
                sbSocketServer.append("<td>")
                        .append(XmlUtil.getXmlElement("rct", xmlStatItem))
                        .append("</td>");
                sbSocketServer.append("</tr>");
            }
            else if (type.equalsIgnoreCase(IStatistics.StatType_Proc))
            {
                sbProc.append("<tr bgcolor='#F4F3EE'>");
                // 模块名
                sbProc.append("<td>")
                        .append(XmlUtil.getXmlElement("mod", xmlStatItem))
                        .append("</td>");
                // 业务ID
                sbProc.append("<td>")
                        .append(XmlUtil.getXmlElement("id", xmlStatItem))
                        .append("</td>");
                // 当前并发请求数
                sbProc.append("<td>")
                        .append(XmlUtil.getXmlElement("act", xmlStatItem))
                        .append("</td>");
                // 最大并发请求数
                sbProc.append("<td>")
                        .append(XmlUtil.getXmlElement("max", xmlStatItem))
                        .append("</td>");
                // 自服务启动以来已成功处理的客户请求数
                long succCnt = Long.parseLong(XmlUtil.getXmlElement("sn",
                        xmlStatItem));
                // 处理总耗时
                long totalCostMills = Long.parseLong(XmlUtil.getXmlElement(
                        "tcm", xmlStatItem));
                // 最大耗时发生时间
                Date maxOccurTime = new Date(Long.parseLong(XmlUtil
                        .getXmlElement("mct", xmlStatItem)));

                sbProc.append("<td>").append(succCnt).append("</td>");
                if (succCnt > 0)
                {
                    sbProc.append("<td>").append(totalCostMills / succCnt)
                            .append("</td>");
                }
                else
                {
                    sbProc.append("<td>").append(totalCostMills)
                            .append("</td>");
                }
                sbProc.append("<td>")
                        .append(XmlUtil.getXmlElement("mcm", xmlStatItem))
                        .append("</td>");
                sbProc.append("<td>").append(df.format(maxOccurTime))
                        .append("</td>");
                // 自服务启动以来处理失败的客户请求数
                sbProc.append("<td>")
                        .append(XmlUtil.getXmlElement("fn", xmlStatItem))
                        .append("</td>");
                sbProc.append("</tr>");
            }
            else if (type.equalsIgnoreCase(IStatistics.StatType_ObjectPool))
            {
                sbObjectPool.append("<tr bgcolor='#F4F3EE'>");
                sbObjectPool.append("<td>")
                        .append(XmlUtil.getXmlElement("alias", xmlStatItem))
                        .append("</td>");
                sbObjectPool.append("<td>")
                        .append(XmlUtil.getXmlElement("id", xmlStatItem))
                        .append("</td>");

                sbObjectPool.append("<td>")
                        .append(XmlUtil.getXmlElement("module", xmlStatItem))
                        .append("</td>");
                sbObjectPool.append("<td>")
                        .append(XmlUtil.getXmlElement("active", xmlStatItem))
                        .append("</td>");
                // sbObjectPool.append("<td>").append(XmlUtil.getXmlElement("min",
                // xmlStatItem)).append("</td>");
                sbObjectPool.append("<td>")
                        .append(XmlUtil.getXmlElement("max", xmlStatItem))
                        .append("</td>");
                sbObjectPool.append("</tr>");
            }
            else if (type.equalsIgnoreCase(IStatistics.StatType_DBConnPool))
            {
                sbDBConnPool.append("<tr bgcolor='#F4F3EE'>");
                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("alias", xmlStatItem))
                        .append("</td>");
                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("id", xmlStatItem))
                        .append("</td>");

                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("module", xmlStatItem))
                        .append("</td>");
                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("active", xmlStatItem))
                        .append("</td>");
                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("alive", xmlStatItem))
                        .append("</td>");
                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("idle", xmlStatItem))
                        .append("</td>");
                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("min", xmlStatItem))
                        .append("</td>");
                sbDBConnPool.append("<td>")
                        .append(XmlUtil.getXmlElement("max", xmlStatItem))
                        .append("</td>");
                sbDBConnPool.append("</tr>");
            }
            else
            {

            }
        }
        sbSocketServer.append("</table>");
        sbProc.append("</table>");
        sbObjectPool.append("</table>");
        sbDBConnPool.append("</table>");

        sb.append("<br>");
        sb.append(sbSocketServer.toString());

        sb.append("<br>");
        sb.append(sbProc.toString());

        sb.append("<br>");
        sb.append(sbObjectPool.toString());

        sb.append("<br>");
        sb.append(sbDBConnPool.toString());

        DateFormat dateFmt = DateFormat.getDateTimeInstance();
        //通讯监控数据
        StringBuilder sbCommData = new StringBuilder();
        sbCommData
                .append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sbCommData.append("<tr bgcolor='#F4F3EE'>");
        sbCommData.append("<td bgcolor='#DFDFDF'>通讯ID</td>");
        sbCommData.append("<td bgcolor='#DFDFDF'>成功次数</td>");
        sbCommData.append("<td bgcolor='#DFDFDF'>失败次数</td>");
        sbCommData.append("<td bgcolor='#DFDFDF'>当前并发</td>");
        sbCommData.append("<td bgcolor='#DFDFDF'>最大并发</td>");
        sbCommData.append("<td bgcolor='#DFDFDF'>平均耗时</td>");
        sbCommData.append("<td bgcolor='#DFDFDF'>最大耗时</td>");
        sbCommData.append("<td bgcolor='#DFDFDF'>最大耗时发生时间</td>");
        sbCommData.append("</tr>");

        String commStatData = XmlUtil.getXmlElement("HostCommData",xmlStatData);

        String[] commDataArray = commStatData.split(",");

        for (String commData : commDataArray)
        {
            String[] dataArray = commData.split("\\|");
            if(dataArray.length >=9)
            {
                sbCommData.append("<tr bgcolor='#F4F3EE'>");
                sbCommData.append("<td>").append(dataArray[0]).append("</td>");
                sbCommData.append("<td>").append(dataArray[4]).append("</td>");
                sbCommData.append("<td>").append(dataArray[5]).append("</td>");
                sbCommData.append("<td>").append(dataArray[1]).append("</td>");
                sbCommData.append("<td>").append(dataArray[2]).append("</td>");

                long execCount = MiscUtil.parseLong(dataArray[4], 1);
                if(execCount<=0) execCount = 1;
                long totalCostMills = MiscUtil.parseLong(dataArray[6], 1);
                sbCommData.append("<td>").append(totalCostMills / execCount).append("</td>");
                sbCommData.append("<td>").append(dataArray[7]).append("</td>");

                Date occurTime = new Date(MiscUtil.parseLong(dataArray[8], 0));
                sbCommData.append("<td>").append(dateFmt.format(occurTime)).append("</td>");

                sbCommData.append("</tr>");
            }
        }

        sbCommData.append("</table>");
        sb.append("<br>");
        sb.append(sbCommData.toString());


        //SQL语句性能监控数据
        StringBuilder sbSqlPerf = new StringBuilder();
        sbSqlPerf
                .append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sbSqlPerf.append("<tr bgcolor='#F4F3EE'>");
        sbSqlPerf.append("<td bgcolor='#DFDFDF' width='60%'>Sql</td>");
        sbSqlPerf.append("<td bgcolor='#DFDFDF'>执行次数</td>");
        sbSqlPerf.append("<td bgcolor='#DFDFDF'>平均耗时</td>");
        sbSqlPerf.append("<td bgcolor='#DFDFDF'>最大耗时</td>");
        sbSqlPerf.append("<td bgcolor='#DFDFDF'>最大耗时发生时间</td>");
        sbSqlPerf.append("</tr>");

        List<String> listxmlSqlPerf = XmlUtil.getAllXmlElements("sqlPerf",
                xmlStatData);


        for (String xmlSqlPerf : listxmlSqlPerf)
        {

            String xmlData = XmlUtil.getXmlElement("data", xmlSqlPerf);
            String[] dataArray = xmlData.split("\\|");
            if (dataArray.length >= 4)
            {
                sbSqlPerf.append("<tr bgcolor='#F4F3EE'>");
                sbSqlPerf.append("<td>")
                        .append(XmlUtil.getXmlElement("sql", xmlSqlPerf))
                        .append("</td>");
                long execCount = MiscUtil.parseLong(dataArray[0], 1);
                long totalCostMills = MiscUtil.parseLong(dataArray[1], 1);

                sbSqlPerf.append("<td>").append(execCount).append("</td>");
                sbSqlPerf.append("<td>").append(totalCostMills / execCount)
                        .append("</td>");
                sbSqlPerf.append("<td>").append(dataArray[2]).append("</td>");

                Date occurTime = new Date(MiscUtil.parseLong(dataArray[3], 0));
                sbSqlPerf.append("<td>").append(dateFmt.format(occurTime))
                        .append("</td>");
                sbSqlPerf.append("</tr>");
            }
        }
        sbSqlPerf.append("</table>");
        sb.append("<br>");
        sb.append(sbSqlPerf.toString());

        //SQL异常监控数据
        StringBuilder sbExpMon = new StringBuilder();
        sbExpMon
                .append("<table width='100%' border='0' cellpadding='1' cellspacing='1' bgcolor='#999999'>");
        sbExpMon.append("<tr bgcolor='#F4F3EE'>");
        sbExpMon.append("<td bgcolor='#DFDFDF'>DataBase</td>");
        sbExpMon.append("<td bgcolor='#DFDFDF'>SQLState</td>");
        sbExpMon.append("<td bgcolor='#DFDFDF'>Message</td>");
        sbExpMon.append("<td bgcolor='#DFDFDF'>发生次数</td>");
        sbExpMon.append("<td bgcolor='#DFDFDF'>最近发生时间</td>");
        sbExpMon.append("</tr>");

        List<String> listxmlSqlExp = XmlUtil.getAllXmlElements("sqlExpMon",
                xmlStatData);
        for (String xmlSqlExp : listxmlSqlExp)
        {
            String dbname = XmlUtil.getXmlElement("dbname", xmlSqlExp);
            List<String> listSqlException = XmlUtil.getAllXmlElements("sqlException",xmlSqlExp);
            for (String sqlExp : listSqlException)
            {
                String[] dataArray = sqlExp.split("\\|");
                if (dataArray.length >= 4)
                {
                    sbExpMon.append("<tr bgcolor='#F4F3EE'>");
                    sbExpMon.append("<td>")
                            .append(dbname)
                            .append("</td>");
                    sbExpMon.append("<td>").append(dataArray[2]).append("</td>");
                    sbExpMon.append("<td>").append(dataArray[3]).append("</td>");

                    long execCount = MiscUtil.parseLong(dataArray[0], 1);
                    sbSqlPerf.append("<td>").append(execCount).append("</td>");

                    Date occurTime = new Date(MiscUtil.parseLong(dataArray[1], 0));
                    sbExpMon.append("<td>").append(dateFmt.format(occurTime))
                            .append("</td>");
                    sbExpMon.append("</tr>");
                }
            }
        }
        sb.append("<br>");
        sb.append(sbExpMon.toString());

        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * 将统计数据输出到Html文件
     *
     * @param htmSrc
     */
    private void dump2Html(String htmSrc)
    {
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(
                    outputFileName));
            out.write(htmSrc);
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String serverIp = "127.0.0.1";
        int serverPort = 8010;
        int loopSeconds = 5;
        String outputFileName = ResourceUtil.getSysDataPath() + "Monitor.htm";

        BServerMonitor bsvrMon = new BServerMonitor(serverIp, serverPort, loopSeconds, outputFileName);
        bsvrMon.start();
    }

}
