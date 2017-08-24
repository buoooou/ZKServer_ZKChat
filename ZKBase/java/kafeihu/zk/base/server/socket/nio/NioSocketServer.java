package kafeihu.zk.base.server.socket.nio;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.server.socket.ISocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 * 基于NIO实现的Thread-on-event模式的SocketServer服务器<br>
 * 适用场景：<br>
 * 1.Socket长连接 <br>
 * 2.高客户端同时在线连接<br>
 * 3.客户端响应体验要求不高
 *
 * Created by zhangkuo on 2017/7/7.
 */
public class NioSocketServer implements ISocketServer{

    /**
     * 服务器端Socket通道
     */
    private ServerSocketChannel m_serverSocketChannel;
    /**
     * 服务器端监听端口
     */
    private final int m_serverPort;
    /**
     * 服务器排队数
     */
    private final int m_backLog;

    /**
     * 客户端Socket操作超时
     */
    private volatile int m_clientSocketTimeoutMills = ISocketServer.DefaultSocketTimeoutMills;
    /**
     * 客户端请求调度分发器
     */
    private final IDispatcher m_dispatcher;
    /**
     * 请求处理器工厂
     */
    private final INioSocketRequestHandlerFactory m_requestHandlerFactory;
    /**
     * 服务器运行状态
     */
    private volatile boolean m_isRunning = true;

    /**
     * 服务器端监听线程
     */
    private Thread m_serverThread;
    /**
     * 事件分发线程
     */
    private Thread m_dispatcherThread;
    /**
     * 日志处理类
     */
    private Logger m_logger;

    /**
     * 构造函数
     *
     * @param port
     * @param dispatcher
     * @param factory
     * @throws IOException
     */
    public NioSocketServer(int port, IDispatcher dispatcher, INioSocketRequestHandlerFactory factory)
            throws Exception, IOException
    {
        this(port, ISocketServer.DefaultServerSocketBackLog, dispatcher, factory, Logger
                .getConsoleLogger());
    }

    /**
     * 构造函数
     *
     * @param port
     * @param dispatcher
     * @param factory
     * @throws IOException
     */
    public NioSocketServer(int port, IDispatcher dispatcher,
                           INioSocketRequestHandlerFactory factory, Logger logger) throws IOException
    {
        this(port, ISocketServer.DefaultServerSocketBackLog, dispatcher, factory, logger);
    }

    /**
     * 构造函数
     *
     * @param port
     * @param backlog
     * @param dispatcher
     * @param factory
     * @throws IOException
     */
    public NioSocketServer(int port, int backlog, IDispatcher dispatcher,
                           INioSocketRequestHandlerFactory factory, Logger logger) throws IOException
    {
        m_serverPort = port;

        m_backLog = backlog;

        m_dispatcher = dispatcher;

        m_requestHandlerFactory = factory;

        m_logger = logger;
    }

    /**
     * 关闭服务器端Socket通道
     *
     */
    private void closeServerSocketChannel()
    {
        try
        {
            m_serverSocketChannel.close();
        }
        catch (Exception exp)
        {
            m_logger.error(getClass().getName(), "cleanup(). exp:" + exp);
        }
    }

    /**
     * 设置接受的客户端Socket连接超时。单位：毫秒
     */
    public void setClientSocketTimeoutMills(int mills)
    {
        if (mills > 0)
        {
            m_clientSocketTimeoutMills = mills;
        }
    }

    /**
     * 服务启动后调用
     */
    public void onServerStarted()
    {
    }

    /**
     * 服务启动时调用
     */
    public void onServerStarting()
    {
    }

    /**
     * 服务停止后调用
     */
    public void onServerStopped()
    {
    }

    /**
     * 服务停止时调用
     */
    public void onServerStopping()
    {
    }

    /**
     * 生命周期方法：在接受客户端Socket连接后调用
     */
    public void onSocketAccepted(Socket socket)
    {
    }

    /**
     * 生命周期方法：关闭客户端Socket连接前调用
     */
    public void onSocketClosing(Socket socket)
    {
    }

    /**
     * 生命周期方法：在客户端Socket关闭后调用
     */
    public void onSocketClosed(Socket socket)
    {
    }

    /**
     * 获取当前在线的Socket客户端数
     */
    public int getOnlineClientNum()
    {
        return m_dispatcher.getRegisteredChannelNum();
    }

    /**
     * 获取日志工具类
     *
     * @return
     */
    public Logger getLogger()
    {
        return m_logger;
    }

    /**
     * 启动服务器
     */
    public Thread start() throws Exception
    {
        // 初始化资源
        initServerSocketChannel();

        onServerStarting();

        // 构造监听线程
        m_serverThread = new Thread(new Listener());
        m_serverThread.setName("NioSocketServerThread. Port:" + m_serverPort);
        // 构造事件分发线程
        m_dispatcherThread = new Thread((Runnable) m_dispatcher);
        m_dispatcherThread.setName("DispatcherThread. SocketServerPort:" + m_serverPort);
        // 设置运行状态
        m_isRunning = true;
        // 启动事件分发线程
        m_dispatcherThread.start();
        // 启动监听线程
        m_serverThread.start();

        onServerStarted();
        return m_serverThread;

    }

    /**
     * 停止服务器
     */
    public void stop() throws Exception
    {
        try
        {
            onServerStopping();
            m_isRunning = false;
            // 清理资源
            cleanup();
            // 终止监听线程
            if ((null != m_serverThread) && (m_serverThread.isAlive()))
            {
                m_serverThread.interrupt();
                m_serverThread.join();

            }
        }
        finally
        {
            onServerStopped();
            m_serverThread = null;
        }
    }

    /**
     * 清理资源，在服务器终止时调用
     */
    private void cleanup()
    {
        // 关闭分发器
        m_dispatcher.shutdown();
        // 关闭服务器端Socket通道
        closeServerSocketChannel();
    }

    /**
     * 初始化资源，SocketServer启动时调用
     *
     * @throws Exception
     */
    private void initServerSocketChannel() throws Exception
    {
        try
        {
            // 构造ServerSocketChannel
            m_serverSocketChannel = ServerSocketChannel.open();
            // 配置为阻塞模式
            m_serverSocketChannel.configureBlocking(true);
            // 设置为永不超时
            m_serverSocketChannel.socket().setSoTimeout(0);
            // 打开SO_REUSEADDR开关
            m_serverSocketChannel.socket().setReuseAddress(true);
            // 设置服务器端Socket属性
            setServerSocketChannelOptions(m_serverSocketChannel);
            // 绑定监听端口
            m_serverSocketChannel.socket().bind(new InetSocketAddress(m_serverPort), m_backLog);
        }
        catch (IOException exp)
        {
            if (null != m_serverSocketChannel)
            {
                m_serverSocketChannel.close();
            }
            throw exp;
        }
    }

    /**
     * 设置服务端SocketChannel属性。子类可可实现该接口，设置ServerSocketChannel的附加属性。
     *
     * @param serverSocketChannel
     * @throws Exception
     */
    protected void setServerSocketChannelOptions(ServerSocketChannel serverSocketChannel)
            throws Exception
    {
    }

    /**
     * 返回服务器监听端口
     */
    public int getLocalPort()
    {
        return m_serverPort;
    }

    /**
     * 返回服务器别名
     */
    public String getAlias()
    {
        return Integer.toString(getLocalPort());
    }

    /**
     * 服务器运行状态
     */
    public boolean isRunning()
    {
        return m_isRunning;
    }

    /**
     * 注册通道到事件分发器失败后调用。默认为关闭通道
     *
     * @param channel
     */
    protected void onChannelRegisterFailed(SocketChannel channel)
    {
        try
        {
            m_logger.error(getClass().getName(), "registerChannel failed : " + channel);
            channel.close();
        }
        catch (Exception exp)
        {
        }
    }


    /**
     * 服务器本地监听线程类
     *
     * @author HO074172
     *
     */
    private class Listener implements Runnable
    {
        public void run()
        {
            try
            {
                while (m_isRunning)
                {
                    // 监听并接受客户端连接
                    SocketChannel clientChannel = m_serverSocketChannel.accept();
                    // 设置客户端Socket超时
                    clientChannel.socket().setSoTimeout(m_clientSocketTimeoutMills);
                    // 调用生命周期方法：子类可覆盖相应的方法，实现特定的操作
                    onSocketAccepted(clientChannel.socket());
                    // 构造读、写数据队列
                    IReadQueue readQueue = m_requestHandlerFactory.createReadQueue();
                    IWriteQueue writeQueue = m_requestHandlerFactory.createWriteQueue();
                    // 注册通道和处理器
                    IChannelFacade channelFacade = m_dispatcher.registerChannel(
                            NioSocketServer.this, clientChannel, m_requestHandlerFactory
                                    .newRequestHandler(), readQueue, writeQueue);
                    if (null == channelFacade)
                    {// 注册失败
                        onChannelRegisterFailed(clientChannel);
                    }
                }
            }
            catch (ClosedByInterruptException e)
            {
                m_logger.error(getClass().getName(), NioSocketServer.this
                        + " ServerSocketChannel closed by interrupt: " + e);
            }
            catch (ClosedChannelException e)
            {
                m_logger.error(getClass().getName(), NioSocketServer.this
                        + " ServerSocketChannel is closed: " + e);
            }
            catch (Throwable t)
            {
                m_logger.error(getClass().getName(), NioSocketServer.this
                        + " Exiting, Unexpected Throwable when doing accept: " + t);
            }
            finally
            {
                try
                {
                    if(m_isRunning)
                    {
                        NioSocketServer.this.stop();
                    }
                }
                catch (Exception exp)
                {
                    m_logger.error(getClass().getName(), NioSocketServer.this
                            + " Unexpected IOException when stop : " + exp);
                }
            }
        }
    }
    @Override
    public String toString()
    {
        return "[alias=" + getAlias() + ", port=" + getLocalPort() + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_serverPort;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NioSocketServer other = (NioSocketServer) obj;
        if (m_serverPort != other.m_serverPort)
            return false;
        return true;
    }

}
