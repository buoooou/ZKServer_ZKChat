package kafeihu.zk.bserver.service.monitor;

import kafeihu.zk.bserver.service.ServiceManager;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 监控模块管理器
 *
 * Created by zhangkuo on 2017/5/30.
 */
public class MonitorManager extends ServiceManager{

    /**
     * 配置文件
     */
    private final static String Config_File_Name = "monitor-config.xml";
    /**
     * 监控服务端口
     */
    private static ServerSocket m_serverSocket;
    /**
     * 监控服务处理类
     */
    private static Map<String, MonitorProc> m_prid = new HashMap<String, MonitorProc>();
    /**
     * 监控服务监听类
     */
    private static MonitorListener m_listener;


    /**
     * 获取指定prid的监控处理类。如果找不到，则抛出异常
     *
     * @param prid
     * @return
     */
    public static MonitorProc getProc(String prid) throws Exception
    {
//        MonitorProc proc = m_prid.get(prid);
//        if (null == proc)
//        {
//            createMonitorProc(prid);
//            proc = m_prid.get(prid);
//            if (null == proc)
//            {
//                throw new Exception("undefined prid:" + prid);
//            }
//        }
//        return proc;
    }

}
