package kafeihu.zk.bserver.monitor.proc;

import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.bserver.core.BserverManager;
import kafeihu.zk.bserver.proc.ResponseData;
import kafeihu.zk.bserver.proc.base.BaseProc;

/**
 * Created by zhangkuo on 2017/6/9.
 */
public class GetStatData extends BaseProc {
    public GetStatData(String mModuleName, String mProcId)
    {
        super(mModuleName, mProcId);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Object doProc(Object reqData) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<timeStamp>").append(MiscUtil.getTimestamp()).append("</timeStamp>");
        // 网关服务启动时间
        sb.append("<sut>").append(BserverManager.getStartTime())
                .append("</sut>");
        // 系统当前时间
        sb.append("<st>").append(System.currentTimeMillis()).append("</st>");
        //附加统计对象数据
//        List<Object> statDataList = StatisticsManager.getStaticticsData();
//        for (Object statData : statDataList)
//        {
//            sb.append((String) statData);
//        }
//        //附加通讯统计数据
//        sb.append(HostCommManager.getXmlCommData());
//        //附加数据库监控数据
//        sb.append(SqlPerfMonitor.getXmlPerfData());
//        //附加数据库异常监控数据
//        sb.append(SQLExceptionMonitor.getXmlSQLExceptionData());
        ResponseData respData = new ResponseData();
        respData.setData(sb.toString().getBytes());
        return respData;
    }

}
