package kafeihu.zk.bserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import kafeihu.zk.base.server.IServer;

public class NettyBootstrap implements IServer {

    static final int PORT = Integer.parseInt(System.getProperty("port", "1080"));

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void onServerStarting() {

    }

    @Override
    public void start() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new SocksServerInitializer());
            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    public void onServerStarted() {

    }

    @Override
    public void onServerStopping() {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void onServerStopped() {

    }

    @Override
    public String getAlias() {
        return null;
    }

    @Override
    public int getOnlineClientNum() {
        return 0;
    }
}
