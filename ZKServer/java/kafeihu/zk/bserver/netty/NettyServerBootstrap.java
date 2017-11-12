package kafeihu.zk.bserver.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import kafeihu.zk.base.server.IServer;
import kafeihu.zk.bserver.manager.Slf4JManager;
import kafeihu.zk.bserver.statistics.IStatistics;
import org.slf4j.Logger;

public class NettyServerBootstrap implements IServer {

    private static int m_serverPort ;

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

    private final ServerBootstrap b = new ServerBootstrap();
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ChannelFuture future;

    @Override
    public boolean isRunning() {
        return m_isRunning;
    }

    @Override
    public void start() throws Exception {

        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new SocksServerInitializer());
        future=b.bind(m_serverPort).syncUninterruptibly();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    m_logger.info("operation Completed!");
                }else {
                    m_logger.error("operation Error: "+future.cause().getMessage());
                }
            }
        });

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

        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
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



}
