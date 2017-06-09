package kafeihu.zk.bserver.socket;

import kafeihu.zk.bserver.service.ServiceManager;
import kafeihu.zk.base.server.socket.ISocketServer;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangkuo on 2016/11/24.
 */
public class SocketServerManager extends ServiceManager {
    /**
     * 配置文件名
     */
    private final static String Config_File_Name = "socketserver-config.xml";
    /**
     * Socket服务器运行实例
     */
    private static Map<Integer, ISocketServer> m_SocketServerInstance = new HashMap<Integer, ISocketServer>();

    private SocketServerManager()
    {
        super();
    }

    /**
     * 根据配置文件，构造Socket服务器实例
     *
     * @param xmlConfig
     * @return
     * @throws Exception
     */
    private static ISocketServer createSocketServer(String xmlConfig) throws Exception
    {
        return SocketServerUtil.createSocketServer(xmlConfig);
    }

    /**
     * 获取所有的Socket服务器配置
     *
     * @return
     * @throws Exception
     */
    private static List<String> getSocketServerConfig() throws Exception
    {
        String configData = ResourceUtil.getSysDataResourceContent(Config_File_Name);
        List<String> listServerConfig = XmlUtil.getAllXmlElements("socketServer", configData);
        return listServerConfig;

    }

    /**
     * 获取指定端口的Socket服务器配置
     *
     * @param port
     * @return
     * @throws Exception
     */
    private static String getSocketServerConfig(int port) throws Exception
    {
        List<String> listServerConfig = getSocketServerConfig();
        for (String serverConfig : listServerConfig)
        {
            String serverSocketConfig = XmlUtil.getXmlElement("serverSocket", serverConfig);
            Integer tmpPort = MiscUtil.toInteger(XmlUtil.getXmlElement("port", serverSocketConfig));
            if (null != tmpPort)
            {
                if (tmpPort.intValue() == port)
                {
                    return serverConfig;
                }
            }
        }
        return null;
    }

    /**
     * 停止特定端口上的Socket服务器
     *
     * @param port
     */
    private static void stopSocketServer(int port) throws Exception
    {
        ISocketServer socketServer = m_SocketServerInstance.get(Integer.valueOf(port));
        if (null != socketServer)
        {
            try
            {
                socketServer.stop();
            }
            catch (Exception exp)
            {
                throw new Exception(SocketServerManager.class.getName() + ".stopSocketServer failed. " + exp);
            }
            finally
            {
                // StatisticsManager.unRegister(socketServer);
                // m_TcpServerInstance.remove(new Integer(port));
                // socketServer = null;
            }
        }
    }

    /**
     * 启动特定的Socket服务器实例
     *
     * @param socketServer
     * @throws Exception
     */
    private static void startSocketServer(ISocketServer socketServer) throws Exception
    {
        // 先停止端口上的服务
        try
        {
            stopSocketServer(socketServer.getLocalPort());
        }
        catch (Exception e)
        {
        }

        socketServer.start();
        m_SocketServerInstance.put(Integer.valueOf(socketServer.getLocalPort()), socketServer);
//        if (socketServer instanceof IStatistics)
//        {
//            StatisticsManager.register((IStatistics) socketServer);
//        }
    }

    /**
     * 获取监听指定端口的Socket服务器实例
     *
     * @param port
     * @return
     */
    public static synchronized ISocketServer getSocketServer(Integer port)
    {
        return m_SocketServerInstance.get(port);
    }

    /**
     * 停止服务
     *
     * @param param
     *            启动参数。Integer实例，指定监听端口
     */
    public static synchronized void stopService(Object param) throws Exception
    {
        if (null == param)
        {
            throw new Exception(SocketServerManager.class.getName() + ".stopService : param can not be null");
        }
        if (param instanceof Integer)
        {
            Integer port = (Integer) param;
            stopSocketServer(port.intValue());
        }
        else
        {
            throw new Exception(SocketServerManager.class.getName() + ".stopService : param must be Integer");
        }

    }

    /**
     * 启动服务
     *
     * @param param
     *            启动参数。Integer实例，指定监听端口
     */
    public static synchronized void startService(Object param) throws Exception
    {
        if (null == param)
        {
            throw new Exception(SocketServerManager.class.getName() + ".startService : param can not be null");
        }
        if (param instanceof Integer)
        {
            Integer port = (Integer) param;

            String serverConfig = getSocketServerConfig(port.intValue());
            if (null == serverConfig)
            {
                throw new Exception(SocketServerManager.class.getName()
                        + ".startService : no socketServer defined at port " + port);
            }
            ISocketServer socketServer = createSocketServer(serverConfig);
            startSocketServer(socketServer);
        }
        else
        {
            throw new Exception(SocketServerManager.class.getName() + ".startService : param must be Integer");
        }
    }

    /**
     * 启动服务
     */
    public static synchronized void startService() throws Exception
    {
        List<String> listServerConfig = getSocketServerConfig();

        for (String serverConfig : listServerConfig)
        {
            ISocketServer socketServer = createSocketServer(serverConfig);
            if(m_SocketServerInstance.containsKey(Integer.valueOf(socketServer.getLocalPort())))
            {
                throw new Exception("duplicate SocketServer defined with port="+socketServer.getLocalPort());
            }
            startSocketServer(socketServer);
        }
    }

    /**
     * 停止服务
     */
    public static synchronized void stopService() throws Exception
    {
        ISocketServer[] socketServerArray = m_SocketServerInstance.values().toArray(new ISocketServer[0]);
        for (ISocketServer socketServer : socketServerArray)
        {
            stopSocketServer(socketServer.getLocalPort());
        }

    }
}
