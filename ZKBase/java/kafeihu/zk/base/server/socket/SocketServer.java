package kafeihu.zk.base.server.socket;

import kafeihu.zk.base.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于Socket实现的Thread-per-connection模式的SocketServer服务器<br>
 * 适用场景：<br>
 * 1.Socket短连接 <br>
 * 2.低客户端同时在线数量<br>
 * 3.高客户端响应体验
 * Created by zhangkuo on 2016/11/25.
 */
public class SocketServer implements ISocketServer{
    /**
     * 服务器端Socket
     */
    private ServerSocket m_serverSocket;
    /**
     * 服务器端监听端口
     */
    private final int m_serverPort;
    /**
     * 服务器排队数
     */
    private final int m_backLog;
    /**
     * 当前在线的Socket客户端连接数
     */
    private final AtomicInteger m_onlineClientNum = new AtomicInteger(0);
    /**
     * Socket超时
     */
    private volatile int m_clientSocketTimeoutMills = ISocketServer.DefaultSocketTimeoutMills;
    /**
     * 服务器运行状态
     */
    private volatile boolean m_isRunning = true;
    /**
     * 服务器端监听线程
     */
    private Thread m_serverThread;

    /**
     * 关联的Socket请求处理器
     */
    private List<ISocketRequestHandler> m_requestHandlerList = new ArrayList<ISocketRequestHandler>();
    /**
     * 关联的Socket请求处理异常处理器
     */
    private ISocketExceptionHandler m_exceptionHandler;
    /**
     * Socket请求工作线程池，当前服务器所接收到的Socket请求的处理线程由其统一调度处理
     */
    private final SocketWorkerPool m_socketWorkerPool;
    /**
     * SSL 上下文
     */
    private SSLContext m_sslContext;

    /**
     * 日志处理类
     */
    private Logger m_logger;

    /**
     * 构造函数 。默认backlog为50，reuseAddress为true，使用默认日志处理类
     *
     * @param port
     * @param socketWorkerPool
     *            Socket工作线程池
     * @throws IOException
     * @throws Exception
     */
    public SocketServer(int port, SocketWorkerPool socketWorkerPool) throws IOException, Exception
    {
        this(port, socketWorkerPool, null, Logger.getConsoleLogger());
    }

    /**
     * 构造函数。默认backlog为50，reuseAddress为true
     *
     * @param port
     * @param socketWorkerPool
     *            Socket工作线程池
     * @param logger
     *            日志处理类
     * @throws IOException
     */
    public SocketServer(int port, SocketWorkerPool socketWorkerPool, Logger logger)
            throws IOException
    {
        this(port, socketWorkerPool, null, logger);
    }

    /**
     * 构造函数。默认backlog为50，reuseAddress为true。支持SSL
     *
     * @param port
     * @param socketWorkerPool
     * @param sslContext
     * @throws IOException
     */
    public SocketServer(int port, SocketWorkerPool socketWorkerPool, SSLContext sslContext,
                        Logger logger) throws IOException
    {
        this(port, ISocketServer.DefaultServerSocketBackLog, socketWorkerPool, sslContext, logger);
    }

    /**
     * 构造函数
     *
     * @param port
     *            监听端口
     * @param backlog
     *            连接请求队列最大值
     * @param socketWorkerPool
     *            Socket工作线程池
     * @param sslContext
     *            SSL上下文
     * @param logger
     *            日志
     * @throws IOException
     */
    public SocketServer(int port, int backlog, SocketWorkerPool socketWorkerPool,
                        SSLContext sslContext, Logger logger) throws IOException
    {
        m_serverPort = port;

        m_backLog = backlog;

        m_socketWorkerPool = socketWorkerPool;

        m_sslContext = sslContext;

        m_logger = logger;
    }

    /**
     * 关闭服务器端Socket
     *
     * @param
     */
    private void closeServerSocket()
    {
        try
        {
            m_serverSocket.close();
        }
        catch (Exception exp)
        {
            m_logger.error(getClass().getName(), "cleanup(). exp:" + exp);
        }
    }

    /**
     * 增加Socket请求处理器
     *
     * @param requestHandler
     */
    public synchronized void addSocketRequestHandler(ISocketRequestHandler requestHandler)
    {
        m_requestHandlerList.add(requestHandler);
    }

    /**
     * 设置Socket请求处理异常处理器
     *
     * @param handler
     */
    public void setExceptionHandler(ISocketExceptionHandler handler)
    {
        m_exceptionHandler = handler;
    }

    /**
     * 返回服务器监听端口
     */
    public int getLocalPort()
    {
        return m_serverPort;
    }

    /**
     * 获取服务器端Socket对象
     *
     * @return
     */
    public ServerSocket getServerSocket()
    {
        return m_serverSocket;
    }

    /**
     * 获取SSL上下文对象
     *
     * @return
     */
    public SSLContext getSSLContext()
    {
        return m_sslContext;
    }

    /**
     * 返回服务器别名
     */
    public String getAlias()
    {
        return Integer.toString(getLocalPort());
    }

    /**
     * 返回Socket工作线程池
     *
     * @return
     */
    public SocketWorkerPool getSocketWorkerPool()
    {
        return m_socketWorkerPool;
    }

    /**
     * 返回服务器运行状态
     */
    public boolean isRunning()
    {
        return m_isRunning;
    }

    /**
     * 设置接受的客户端Socket连接超时。单位：毫秒
     */
    public void setClientSocketTimeoutMills(int mills)
    {
        if (mills >= 0)
        {
            m_clientSocketTimeoutMills = mills;
        }
    }

    /**
     * 获取当前在线的Socket客户端数
     */
    public int getOnlineClientNum()
    {
        return m_onlineClientNum.get();
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
        m_onlineClientNum.incrementAndGet();
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
        m_onlineClientNum.decrementAndGet();
    }

    /**
     * 启动SocketServer
     */
    public void start() throws Exception
    {
        // 初始化服务器端Socket
        initServerSocket();

        onServerStarting();
        // 构造监听线程
        m_serverThread = new Thread(new Listener());
        // 设置线程名称
        m_serverThread.setName("SocketServerThread. Port:" + m_serverPort);
        // 设置运行状态
        m_isRunning = true;
        // 启动监听线程
        m_serverThread.start();

        onServerStarted();
    }

    /**
     * 停止SocketServer
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
     * 设置服务端Socket属性。子类可可实现该接口，设置ServerSocket的附加属性。
     *
     * @param serverSocket
     * @throws Exception
     */
    protected void setServerSocketOptions(ServerSocket serverSocket) throws Exception
    {
    }

    /**
     * 初始化服务端Socket，SocketServer启动时调用
     *
     * @throws Exception
     */
    private void initServerSocket() throws Exception
    {
        try
        {
            // 构造未绑定的ServerSocket实例
            if (null == m_sslContext)
            {
                // 不支持SSL
                m_serverSocket = new ServerSocket();
            }
            else
            {
                // 支持SSL
                m_serverSocket = (SSLServerSocket) m_sslContext.getServerSocketFactory()
                        .createServerSocket();
                // ((SSLServerSocket) ss).setNeedClientAuth(m_needAuthClient);
            }
            // 设为永不超时
            m_serverSocket.setSoTimeout(0);
            // 打开SO_REUSEADDR开关
            m_serverSocket.setReuseAddress(true);
            // 设置服务器端Socket属性
            setServerSocketOptions(m_serverSocket);
            // 绑定服务器端Socket到监听端口
            m_serverSocket.bind(new InetSocketAddress(m_serverPort), m_backLog);
        }
        catch (Exception exp)
        {
            if (null != m_serverSocket)
            {
                m_serverSocket.close();
            }
            throw exp;
        }
    }

    /**
     * 清理资源，在SocketServer终止时调用
     */
    protected void cleanup()
    {
        // 关闭服务器端Socket
        closeServerSocket();

        try
        {
            // 按顺序执行已提交的任务，不再接受新的任务
            m_socketWorkerPool.shutdown();
            // 等待所有任务结束
            int cnt = 2;
            while ((!m_socketWorkerPool.awaitTermination(10, TimeUnit.SECONDS)) && (cnt < 10))
            {
                cnt++;
            }
        }
        catch (InterruptedException exp)
        {
        }

    }

    /**
     * 构造一个新的SocketWorker实例
     *
     * @param clientSocket
     *            客户端Socket连接
     * @param reqHandlerList
     *            请求处理器列表
     * @param expHandler
     *            请求处理异常处理器
     * @return
     */
    protected SocketWorker newSocketWorker(Socket clientSocket,
                                           List<ISocketRequestHandler> reqHandlerList, ISocketExceptionHandler expHandler)
    {
        return new SocketWorker(this, clientSocket, reqHandlerList, expHandler);
    }

    /**
     * SocketServer本地监听线程
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
                    Socket clientSocket = m_serverSocket.accept();


                    // 设置客户端Socket超时
                    clientSocket.setSoTimeout(m_clientSocketTimeoutMills);



                    // 调用生命周期方法：子类可覆盖相应的方法，实现特定的操作
                    onSocketAccepted(clientSocket);

                    // 构造Socket工作类
                    SocketWorker worker = newSocketWorker(clientSocket, m_requestHandlerList,
                            m_exceptionHandler);
                    // 加入线程池调度执行

                    m_socketWorkerPool.execute(worker);

                }
            }
            catch (IOException exp)
            {
                if (m_isRunning)
                {
                    m_logger.error(getClass().getName(),
                            SocketServer.this+" Exiting, Unexpected IOException when doing accept: " + exp);
                }
            }
            catch (Throwable t)
            {
                m_logger.error(getClass().getName(),
                        SocketServer.this+" Exiting, Unexpected Throwable when doing accept: " + t);
            }
            finally
            {
                try
                {
                    if(m_isRunning)
                    {
                        SocketServer.this.stop();
                    }
                }
                catch (Exception exp)
                {
                    m_logger.error(getClass().getName(),
                            SocketServer.this+" Unexpected IOException when stop : " + exp);
                }
                // cleanup();
                // m_isRunning = false;
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
        SocketServer other = (SocketServer) obj;
        if (m_serverPort != other.m_serverPort)
            return false;
        return true;
    }
}
