package kafeihu.zk.bserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import kafeihu.zk.base.server.IServer;
import kafeihu.zk.bserver.manager.Slf4JManager;
import org.slf4j.Logger;

public class NettyServerBootstrap implements IServer {

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
    EventLoopGroup m_bossGroup ;
    EventLoopGroup m_workerGroup ;
    private ChannelFuture future;

    private SocksServerInitializer pipelineFactory = new SocksServerInitializer();

    @Override
    public boolean isRunning() {
        return m_isRunning;
    }

    public void setPipelineFactory(SocksServerInitializer pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
    }

    @Override
    public void start() throws Exception {

        m_bossGroup = new NioEventLoopGroup(m_config.getAcceptorThreadNum());
        m_workerGroup = new NioEventLoopGroup(m_config.getWorkerThreadNum());


            m_bootStrap = new ServerBootstrap();

            m_bootStrap.group(m_bossGroup, m_workerGroup)
                    .channel(NioServerSocketChannel.class);
            if (m_config.getAcceptorChannelHandlerFactory() != null)
            {
                m_bootStrap.handler(m_config.getAcceptorChannelHandlerFactory().newHandler());
            }
            m_bootStrap.childHandler(m_config.getWorkerChannelHandlerFactory().newHandler());

            future = m_bootStrap.bind(m_config.getPort()).syncUninterruptibly();
            applyConnectionOptions(m_bootStrap);

            Channel ch = m_bootStrap.bind().sync().channel();

            System.out.println("SocketServer started. Port:" + m_config.getPort());

            ch.closeFuture().sync();

    }

    public void dumpConfig()
    {
        ServerBootstrapConfig bc = m_bootStrap.config();
        System.out.println("group:"+bc.group()+" threadNum:"+((NioEventLoopGroup)bc.group()).executorCount());
        System.out.println("childGroup:"+bc.childGroup()+" threadNum:"+((NioEventLoopGroup)bc.childGroup()).executorCount());
        System.out.println("handler:"+bc.handler());
        System.out.println("childHandler:"+bc.childHandler());
        System.out.println("options:"+bc.options());
        System.out.println("childOptions:"+bc.childOptions());

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

    @Override
    public String getAlias() {
        return m_alias;
    }

    @Override
    public int getOnlineClientNum() {
        return 0;
    }

    /**
     * 根据配置文件构造Socket服务器实例
     *
     * @param xmlConfig
     * @return
     */
    public static NettyServerBootstrap buildSocketServer(String xmlConfig) throws Exception
    {
        return new NettyServerBootstrap();
    }

}
