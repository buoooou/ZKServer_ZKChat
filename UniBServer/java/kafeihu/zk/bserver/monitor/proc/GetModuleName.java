package kafeihu.zk.bserver.monitor.proc;

import kafeihu.zk.bserver.manager.ContextManager;
import kafeihu.zk.bserver.manager.ModuleManager;

import java.net.InetAddress;
import java.util.List;

/**
 *
 * Created by zhangkuo on 2017/6/9.
 */
public class GetModuleName extends MonitorProc{

    @Override
    protected void logMonitorResult(InetAddress monitor, String result)
    {// 不需要记录监控结果
    }

    @Override
    protected String procRequest(String request) throws Exception
    {
        List<String> moduleNameList = ModuleManager.getModuleName();
        StringBuilder sb = new StringBuilder();

        sb.append(ContextManager.getApplicationContext().getApplicationName());

        for (String moduleName : moduleNameList)
        {
            sb.append(",").append(moduleName);
        }
        return sb.toString();
    }

}
