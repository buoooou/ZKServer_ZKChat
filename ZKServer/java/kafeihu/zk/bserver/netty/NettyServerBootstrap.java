package kafeihu.zk.bserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import kafeihu.zk.base.server.IServer;
import kafeihu.zk.bserver.manager.Slf4JManager;
import org.slf4j.Logger;

public class NettyServerBootstrap implements IServer {

    private final int PORT = Integer.parseInt(System.getProperty("port", "1080"));

    private static final Logger m_logger = Slf4JManager.getSysLogger();
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

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();


    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void start() throws Exception {

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new SocksServerInitializer());
        b.bind(PORT).sync().channel().closeFuture().sync();


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

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public String getAlias() {
        return m_alias;
    }

    @Override
    public int getOnlineClientNum() {
        return 0;
    }
}
