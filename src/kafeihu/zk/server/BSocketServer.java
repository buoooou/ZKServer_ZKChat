package kafeihu.zk.server;

import kafeihu.zk.server.config.ThreadPoolConfig;
import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.manager.LoggerManager;
import kafeihu.zk.server.socket.SocketWorkerPool;
import kafeihu.zk.server.socket.SocketWorkerRejectedExecutionHandler;
import kafeihu.zk.server.socket.handler.ISocketExceptionHandler;
import kafeihu.zk.server.socket.handler.ISocketRequestHandler;
import kafeihu.zk.base.statistics.IStatistics;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.socket.util.SocketServerUtil;
import kafeihu.zk.base.util.XmlUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class BSocketServer extends SocketServer implements IStatistics {
    private static final Logger m_logger = LoggerManager.getSysLogger();
    /**
     * 服务器别名
     */
    private String m_alias;
    /**
     * 服务器启动时间
     */
    private long m_startUpTime = System.currentTimeMillis();
    /**
     * 服务器停止时间
     */
    private long m_stopTime = -1;
    /**
     * 服务端Socket属性
     */
    private Map<String, String> m_serverOptions = new HashMap<String, String>();
    /**
     * 是否支持SSL
     */
    private boolean m_sslOn = false;

    /**
     * 构造函数
     *
     * @param port
     * @param socketWorkerPool
     * @throws IOException
     */
    public BSocketServer(int port, SocketWorkerPool socketWorkerPool) throws IOException
    {
        this(port, socketWorkerPool, null);
    }

    /**
     * 构造函数
     *
     * @param port
     * @param socketWorkerPool
     * @param sslContext
     *            SSL上下文
     * @throws IOException
     */
    public BSocketServer(int port, SocketWorkerPool socketWorkerPool, SSLContext sslContext)
            throws IOException
    {
        this(port, ISocketServer.DefaultServerSocketBackLog, socketWorkerPool, sslContext);
    }

    /**
     * 构造函数
     *
     * @param port
     * @param backlog
     * @param socketWorkerPool
     * @param sslContext
     *            SSL上下文
     * @throws IOException
     */
    public BSocketServer(int port, int backlog, SocketWorkerPool socketWorkerPool,
                         SSLContext sslContext) throws IOException
    {
        super(port, backlog, socketWorkerPool, sslContext, m_logger);

        if (null != sslContext)
        {
            m_sslOn = true;
        }
    }

    public void setAlias(String m_alias)
    {
        this.m_alias = m_alias;
    }

    /**
     * 增加ServerSocket属性配置
     *
     * @param name
     *            属性名
     * @param value
     *            属性值
     * @throws Exception
     *             如果设置属性失败
     *
     */
    public void addServerOptionConfig(String name, String value) throws Exception
    {
        if (name.equals(ISocketServer.SO_RCVBUF))
        {
            m_serverOptions.put(name, value);
        }
        else if (name.equals(ISocketServer.SO_REUSEADDR))
        {
            m_serverOptions.put(name, value);
        }
        else if (name.equals(ISocketServer.SO_NEEDCLIENTAUTH))
        {
            m_serverOptions.put(name, value);
        }
        else
        {
            throw new Exception("option not supported : " + name);
        }
    }

    /**
     * 设置ServerSocket附加属性
     */
    @Override
    protected void setServerSocketOptions(ServerSocket serverSocket) throws Exception
    {

        // 设置ServerSocket属性
        Set<String> optionKeyset = m_serverOptions.keySet();
        for (String optionKey : optionKeyset)
        {
            String optionValue = m_serverOptions.get(optionKey);
            if (optionKey.equals(ISocketServer.SO_RCVBUF))
            {
                serverSocket.setReceiveBufferSize(Integer.parseInt(optionValue));
            }
            else if (optionKey.equals(ISocketServer.SO_REUSEADDR))
            {
                boolean reuseAddress = optionValue.equalsIgnoreCase("Y")
                        || optionValue.equalsIgnoreCase("true");
                serverSocket.setReuseAddress(reuseAddress);
            }
            else if (optionKey.equals(ISocketServer.SO_NEEDCLIENTAUTH))
            {
                if (m_sslOn)
                {
                    boolean needClientAuth = optionValue.equalsIgnoreCase("Y")
                            || optionValue.equalsIgnoreCase("true");
                    ((SSLServerSocket) serverSocket).setNeedClientAuth(needClientAuth);
                }
            }
        }
    }

    @Override
    public void onServerStarting()
    {
        super.onServerStarting();

        m_startUpTime = System.currentTimeMillis();

    }

    @Override
    public void onServerStarted()
    {
        super.onServerStarted();
        m_logger.info(getClass().getName(), "started. " + toString());

    }

    @Override
    public void onServerStopped()
    {
        // TODO Auto-generated method stub
        super.onServerStopped();
        m_stopTime = System.currentTimeMillis();
        m_logger.info(getClass().getName(), "stopped. " + toString());
    }

    /**
     * 返回统计数据
     */
    public Object getStatistics()
    {

        SocketWorkerPool workerPool = getSocketWorkerPool();

        SocketWorkerRejectedExecutionHandler rejectedHandler = (SocketWorkerRejectedExecutionHandler) workerPool
                .getRejectedExecutionHandler();

        StringBuilder sb = new StringBuilder();
        sb.append("<statistics>");
        // 统计数据类型：SocketServer
        sb.append("<type>").append(IStatistics.StatType_SocketServer).append("</type>");
        // SocketServer别名
        sb.append("<alias>").append(m_alias).append("</alias>");
        // 监听端口
        sb.append("<port>").append(getLocalPort()).append("</port>");
        // 当前在线客户（Socket连接）数
        sb.append("<ocn>").append(getOnlineClientNum()).append("</ocn>");
        // 自服务启动以来已处理的客户请求数
        sb.append("<rn>").append(workerPool.getCompletedTaskCount()).append("</rn>");
        // 自服务启动以来拒绝处理的客户请求数
        sb.append("<rjn>").append(rejectedHandler.getRejectedExecutionCount()).append("</rjn>");
        // 最大并发线程数
        sb.append("<max>").append(workerPool.getLargestPoolSize()).append("</max>");
        // 当前活跃线程数
        sb.append("<act>").append(workerPool.getActiveCount()).append("</act>");
        // 最大允许并发线程数
        sb.append("<rct>").append(workerPool.getMaximumPoolSize()).append("</rct>");
        // 服务启动时间
        sb.append("<sut>").append(m_startUpTime).append("</sut>");
        // 服务器当前时间
        // sb.append("<st>").append(System.currentTimeMillis()).append("</st>");
        if (isRunning())
        {
            sb.append("<state>run</state>");
        }
        else
        {
            sb.append("<state>stop</state>");
            sb.append("<stt>").append(m_stopTime).append("</stt>");
        }
        sb.append("</statistics>");
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "[alias=" + m_alias + ", port=" + getLocalPort() + "]";
    }

    @Override
    public boolean equals(Object obj)
    {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * 根据配置文件构造Socket服务器实例
     *
     * @param xmlConfig
     * @return
     */
    public static ISocketServer buildSocketServer(String xmlConfig) throws Exception
    {
        // 解析服务器ServerSocket配置
        String serverSocketXmlConfig = XmlUtil.getXmlElement("serverSocket", xmlConfig);
        if (MiscUtil.isEmpty(serverSocketXmlConfig))
        {
            throw new Exception("serverSocket can not be empty");
        }
        String tmpValue = "";
        // 解析服务器监听端口
        int serverPort;
        try
        {
            tmpValue = XmlUtil.getXmlElement("port", serverSocketXmlConfig);
            serverPort = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal serverSocket/port value:" + tmpValue);
        }

        // 解析请求队列大小
        int serverBacklog = DefaultServerSocketBackLog;
        try
        {
            tmpValue = XmlUtil.getXmlElement("backlog", serverSocketXmlConfig, "50");
            serverBacklog = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal serverSocket/backlog value:" + tmpValue);
        }
        // 解析客户端Socket连接超时
        int clientTimeoutMills;
        try
        {
            tmpValue = XmlUtil.getXmlElement("clientSocketTimeoutMills", serverSocketXmlConfig,
                    "15000");
            clientTimeoutMills = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal serverSocket/clientSocketTimeoutMills value:" + tmpValue);
        }
        // 解析SSL上下文配置
        SSLContext sslContext = null;

//        String xmlSSLContextConfig = XmlUtil.getXmlElement("sslContext", serverSocketXmlConfig);
//        if (!MiscUtil.isEmpty(xmlSSLContextConfig))
//        {
//            SSLContextConfig sslContextConfig = SSLContextConfig
//                    .parseSSLContextConfig(xmlSSLContextConfig);
//            sslContext = SecurityUtil.createSSLContext(sslContextConfig);
//        }

        // 解析线程池配置
        String threadPoolXmlConfig = XmlUtil.getXmlElement("threadPool", xmlConfig);
        if (MiscUtil.isEmpty(threadPoolXmlConfig))
        {
            throw new Exception("threadPool can not be empty");
        }
        // 解析并构造线程池实例
        ThreadPoolConfig threadPoolConfig = ThreadPoolConfig
                .parseThreadPoolConfig(threadPoolXmlConfig);
        SocketWorkerPool workerPool = SocketServerUtil.createSocketWorkerPool(threadPoolConfig);

        // 构造SocketServer实例
        BSocketServer socketServer = new BSocketServer(serverPort, serverBacklog, workerPool,
                sslContext);
        // 设置客户端Socket连接超时
        socketServer.setClientSocketTimeoutMills(clientTimeoutMills);
        // 解析并设置SocketServer选项值
        String serverOptionsConfig = XmlUtil.getXmlElement("options", serverSocketXmlConfig);
        List<String> optionList = XmlUtil.getAllXmlElementName(serverOptionsConfig);
        for (String optionName : optionList)
        {
            String optionValue = XmlUtil.getXmlElement(optionName, serverOptionsConfig);
            socketServer.addServerOptionConfig(optionName, optionValue);
        }

        // 解析SocketWorker及请求处理器配置
        String socketWorkerConfig = XmlUtil.getXmlElement("socketWorker", xmlConfig);
        List<String> requestHandlerConfigList = XmlUtil.getAllXmlElements("requestHandler",
                socketWorkerConfig);
        if (requestHandlerConfigList.size() <= 0)
        {
            throw new Exception("socketWorker/requestHandler can not be empty");
        }
        for (String requestHandlerConfig : requestHandlerConfigList)
        {
            ISocketRequestHandler requestHandler = SocketServerUtil
                    .createSocketRequestHandler(requestHandlerConfig);
            socketServer.addSocketRequestHandler(requestHandler);
        }

        String exceptionHandlerConfig = XmlUtil.getXmlElement("exceptionHandler",
                socketWorkerConfig);
        if (MiscUtil.isEmpty(exceptionHandlerConfig))
        {
            throw new Exception("socketWorker/exceptionHandler can not be empty");
        }
        ISocketExceptionHandler exceptionHandler = SocketServerUtil
                .createSocketExceptionHandler(exceptionHandlerConfig);
        socketServer.setExceptionHandler(exceptionHandler);

        // 解析其他配置
        String alias = XmlUtil.getXmlElement("alias", xmlConfig, "SocketServer:" + serverPort);
        socketServer.setAlias(alias);

        return socketServer;
    }

}
