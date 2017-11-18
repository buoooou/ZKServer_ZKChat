package kafeihu.zk.bserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import kafeihu.zk.base.server.IServer;
import kafeihu.zk.bserver.manager.Slf4JManager;
import kafeihu.zk.bserver.statistics.IStatistics;
import org.slf4j.Logger;

public class NettyServerBootstrap implements IServer {

    private static int m_serverPort;

    private static final Logger m_logger = Slf4JManager.getSysLogger();
    /**
     * 服务器运行状态
     */
    private volatile boolean m_isRunning = true;
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

    private ServerBootstrap m_bootStrap;
    private NettyServerConfig m_config;

    private ChannelFuture future;

    private SocksServerInitializer pipelineFactory = new SocksServerInitializer();

    @Override
    public boolean isRunning() {
        return m_isRunning;
    }

    public NettyServerBootstrap(int port) {

        this.m_serverPort = port;
    }

    public void setPipelineFactory(SocksServerInitializer pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
    }

    @Override
    public void start() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(m_config.getAcceptorThreadNum());
        EventLoopGroup workerGroup = new NioEventLoopGroup(m_config.getWorkerThreadNum());

        try{
            m_bootStrap = new ServerBootstrap();

            m_bootStrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(pipelineFactory);
            future = m_bootStrap.bind(m_config.getPort()).syncUninterruptibly();
            applyConnectionOptions(m_bootStrap);

            Channel ch = m_bootStrap.bind().sync().channel();

            //���������Ϣ������ʹ��
            //dumpConfig();

            System.out.println("SocketServer started. Port:" + m_config.getPort());
            // System.out.println("Open your web browser and navigate to " +
            // (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        }
        finally
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /***
     *
     * 设置连接参数
     *
     * @param bootstrap 服务器引导
     */
    protected void applyConnectionOptions(ServerBootstrap bootstrap) {
//        SocketConfig config = configCopy.getSocketConfig();
//        bootstrap.childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay());
//        if (config.getTcpSendBufferSize() != -1) {
//            bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getTcpSendBufferSize());
//        }
//        if (config.getTcpReceiveBufferSize() != -1) {
//            bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getTcpReceiveBufferSize());
//            bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(config.getTcpReceiveBufferSize()));
//        }
//        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, config.isTcpKeepAlive());
//        bootstrap.childOption(ChannelOption.SO_LINGER, config.getSoLinger());
//
//        bootstrap.option(ChannelOption.SO_REUSEADDR, config.isReuseAddress());
//        bootstrap.option(ChannelOption.SO_BACKLOG, config.getAcceptBackLog());
    }
    @Override
    public void onServerStarting()
    {
        m_startUpTime = System.currentTimeMillis();
    }

    @Override
    public void onServerStarted()
    {
        m_logger.info(getClass().getName(), "started. " + toString());

    }

    @Override
    public void onServerStopped()
    {
        // TODO Auto-generated method stub
        m_stopTime = System.currentTimeMillis();
        m_logger.info(getClass().getName(), "stopped. " + toString());
    }

    @Override
    public void onServerStopping() {
    }

    @Override
    public void stop() throws Exception {

        m_bossGroup.shutdownGracefully().syncUninterruptibly();
        m_workerGroup.shutdownGracefully().syncUninterruptibly();
        m_logger.info("Socket NettyServer stopped!");
    }
    /**
     * 返回服务器监听端口
     */
    public int getLocalPort()
    {
        return m_serverPort;
    }
    @Override
    public String getAlias() {
        return m_alias;
    }

    @Override
    public int getOnlineClientNum() {
        return 0;
    }

    /**
     * 返回统计数据
     */
    public Object getStatistics()
    {

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
        //sb.append("<rn>").append(workerPool.getCompletedTaskCount()).append("</rn>");
        // 自服务启动以来拒绝处理的客户请求数
        //sb.append("<rjn>").append(rejectedHandler.getRejectedExecutionCount()).append("</rjn>");
        // 最大并发线程数
        //sb.append("<max>").append(workerPool.getLargestPoolSize()).append("</max>");
        // 当前活跃线程数
        //sb.append("<act>").append(workerPool.getActiveCount()).append("</act>");
        // 最大允许并发线程数
        //sb.append("<rct>").append(workerPool.getMaximumPoolSize()).append("</rct>");
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

    /**
     * 根据配置文件构造Socket服务器实例
     *
     * @param xmlConfig
     * @return
     */
    public static NettyServerBootstrap buildSocketServer(String xmlConfig) throws Exception
    {

        int port = 8010;
        return new NettyServerBootstrap(port);
    }

}
