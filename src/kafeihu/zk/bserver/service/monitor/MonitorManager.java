package kafeihu.zk.bserver.service.monitor;

import kafeihu.zk.base.util.IPPattern;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.bserver.service.ServiceManager;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
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

    private MonitorManager()
    {
        super();
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    private static void initialize() throws Exception {
        if (!ResourceUtil.isSysDataResourceExists(Config_File_Name))
        {
            return;
        }
        String configData = ResourceUtil.getSysDataResourceContent(Config_File_Name);
        String tmpValue = XmlUtil.getXmlElement("port", configData);
        int port;
        try
        {
            port = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal monitor port value:" + tmpValue);
        }
        m_serverSocket = new ServerSocket(port);

        m_prid.clear();
        List<String> listPrid = XmlUtil.getAllXmlElements("prid", configData);
        for (String pridConfig : listPrid)
        {
            String id = XmlUtil.getXmlElement("id", pridConfig);
            if (MiscUtil.isEmpty(id))
            {
                throw new Exception("prid/id can not be empty");
            }
            if (m_prid.containsKey(id))
            {
                throw new Exception("duplicate prid/id defined. id=" + id);
            }
            String impl = XmlUtil.getXmlElement("impl", pridConfig);
            if (MiscUtil.isEmpty(impl))
            {
                throw new Exception("prid/impl can not be empty. id=" + id);
            }
            MonitorProc proc = (MonitorProc) Class.forName(impl).newInstance();
            proc.setId(id);
            proc.setParams(pridConfig);
            m_prid.put(id, proc);
        }
        initPrid();
        m_listener = new MonitorListener(m_serverSocket);

        String clientIPControlConfig = XmlUtil.getXmlElement("clientIPControl", configData);
        if (!MiscUtil.isEmpty(clientIPControlConfig))
        {
            // 解析客户端IP匹配规则
            IPPattern ipPattern = new IPPattern(clientIPControlConfig);
            // 允许模式
            boolean allowIP = XmlUtil.getXmlElement("allow", clientIPControlConfig, "Y")
                    .equalsIgnoreCase("Y");

            m_listener.setClientIPPatten(ipPattern);
            m_listener.setAllow(allowIP);
        }

    }

    private static void initPrid()
    {
//        if(!m_prid.containsKey("GetStatistics"))
//        {
//            m_prid.put("GetStatistics", new GetStatistics());
//        }
//        if(!m_prid.containsKey("GetModuleName"))
//        {
//            m_prid.put("GetModuleName", new GetModuleName());
//        }
//        if(!m_prid.containsKey("QuerySysLogData"))
//        {
//            m_prid.put("QuerySysLogData", new QuerySysLogData());
//        }
    }

    /**
     * 解析配置文件，构造指定prid的监控处理类
     *
     * @param prid
     * @return
     * @throws Exception
     */
    private static void createMonitorProc(String prid) throws Exception
    {
        String configData = ResourceUtil.getSysDataResourceContent(Config_File_Name);
        List<String> listPrid = XmlUtil.getAllXmlElements("prid", configData);
        for (String pridConfig : listPrid)
        {
            String id = XmlUtil.getXmlElement("id", pridConfig);
            if (id.equalsIgnoreCase(prid))
            {
                String impl = XmlUtil.getXmlElement("impl", pridConfig);
                if (MiscUtil.isEmpty(impl))
                {
                    throw new Exception("prid/impl can not be empty. id=" + id);
                }
                MonitorProc proc = (MonitorProc) Class.forName(impl).newInstance();
                proc.setId(id);
                proc.setParams(pridConfig);
                //m_prid.put(id, proc);
                m_prid.put(id,proc);
            }
        }

    }
    /**
     * 获取指定prid的监控处理类。如果找不到，则抛出异常
     *
     * @param prid
     * @return
     */
    public static MonitorProc getProc(String prid) throws Exception
    {
        MonitorProc proc = m_prid.get(prid);
        if (null == proc)
        {
            createMonitorProc(prid);
            proc = m_prid.get(prid);
            if (null == proc)
            {
                throw new Exception("undefined prid:" + prid);
            }
        }
        return proc;
    }

    /**
     * 启动监控服务
     */
    public static void startService() throws Exception
    {
        try
        {
            stopService();
        }
        catch (Exception exp)
        {
        }
        initialize();
        if(null != m_listener)
        {
            m_listener.start();
        }

    }

    /**
     * 停止监控服务
     */
    public static void stopService() throws Exception
    {
        if(null != m_listener)
        {
            m_listener.stop();
            m_listener = null;
        }
    }

    /**
     * 停止监控服务
     */
    public static void stopService(Object param) throws Exception
    {
        stopService();
    }

    /**
     * 启动监控服务
     */
    public static void startService(Object param) throws Exception
    {
        startService();
    }
}
