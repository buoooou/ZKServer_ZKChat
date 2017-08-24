package kafeihu.zk.bserver.monitor.proc;

import kafeihu.zk.bserver.core.BserverManager;

import java.net.InetAddress;

/**
 * Created by zhangkuo on 2017/6/9.
 */
public class GetStatistics extends MonitorProc {

    @Override
    protected void logMonitorResult(InetAddress monitor, String result)
    {// 不需要记录监控结果
    }

    @Override
    protected String procRequest(String request) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        //sb.append("<timeStamp>").append(MiscUtil.getTimestamp()).append("</timeStamp>");
        //网关服务启动时间
        sb.append("<sut>").append(BserverManager.getStartTime()).append("</sut>");
        //系统当前时间
        sb.append("<st>").append(System.currentTimeMillis()).append("</st>");
//        List<Object> statDataList = StatisticsManager.getStaticticsData();
//        for (Object statData : statDataList)
//        {
//            sb.append((String) statData);
//        }
        return sb.toString();
    }
}
