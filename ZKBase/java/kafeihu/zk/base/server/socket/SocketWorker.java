package kafeihu.zk.base.server.socket;

import java.net.Socket;
import java.util.List;

/**
 * Socket工作线程类：对每个客户端Socket请求，生成一个单独的工作线程类处理
 * Created by zhangkuo on 2016/11/25.
 */
public class SocketWorker implements Runnable{
    /**
     * 请求处理器列表
     */
    private List<ISocketRequestHandler> m_requestHandlerList;
    /**
     * 异常处理器
     */
    private ISocketExceptionHandler m_exceptionHandler;
    /**
     * 当前Socket连接
     */
    private Socket m_socket;
    /**
     * 关联的Socket服务器
     */
    private SocketServer m_socketServer;

    /**
     * 构造函数
     *
     * @param socketSrv
     *            关联的SocketServer实例
     * @param socket
     *            当前客户端Socket连接
     * @param requestHandlerList
     *            请求处理器列表
     * @param exceptionHandler
     *            异常处理器
     */
    public SocketWorker(SocketServer socketSrv, Socket socket, List<ISocketRequestHandler> requestHandlerList,
                        ISocketExceptionHandler exceptionHandler)
    {
        super();
        m_socketServer = socketSrv;
        m_socket = socket;
        m_requestHandlerList = requestHandlerList;
        m_exceptionHandler = exceptionHandler;
    }

    /**
     * 返回关联的请求处理异常处理器
     *
     * @return
     */
    public ISocketExceptionHandler getExceptionHandler()
    {
        return m_exceptionHandler;
    }

    /**
     * 返回关联的请求处理器列表
     *
     * @return
     */
    public List<ISocketRequestHandler> getRequestHandlerList()
    {
        return m_requestHandlerList;
    }

    /**
     * 返回关联的客户端Socket连接
     *
     * @return
     */
    public Socket getSocket()
    {
        return m_socket;
    }

    /**
     * 返回关联的SocketServer实例
     *
     * @return
     */
    public SocketServer getSocketServer()
    {
        return m_socketServer;
    }

    /**
     * 扩展方法：开始请求处理前调用。子类可覆盖并实现特定处理。
     */
    protected void beforeHandleRequest()
    {
    }

    /**
     * 扩展方法：请求处理结束后调用。子类可覆盖并实现特定处理。
     */
    protected void afterHandleRequest()
    {
    }
    /**
     * 关闭客户端Socket连接
     */
    public final void closeSocket()
    {
        try
        {
            // 调用SocketServer生命周期方法
            m_socketServer.onSocketClosing(m_socket);
            // 关闭Socket连接
            m_socket.close();
        }
        catch (Exception e)
        {
            m_socketServer.getLogger().error(getClass().getName(), e);
        }
        finally
        {
            // 调用SocketServer生命周期方法
            m_socketServer.onSocketClosed(m_socket);
        }
    }

    /**
     * 线程入口方法
     */
    public void run()
    {
        try
        {
            // 请求预处理
            beforeHandleRequest();
            // 依次调用所有的请求处理器
            Object preResult = null;
            for (ISocketRequestHandler reqHandler : m_requestHandlerList)
            {
                preResult = reqHandler.handleRequest(m_socket, preResult);
            }
        }
        catch (EarlyFinishException efe) {
            // 提前终止请求处理线程
        }
        catch (Exception exp)
        {
            // 处理抛出的异常
            exp.printStackTrace();
            m_exceptionHandler.handleException(m_socket, exp);
        }
        finally
        {
            // 请求后处理
            afterHandleRequest();
            // 关闭Socket连接
            closeSocket();
        }

    }
}
