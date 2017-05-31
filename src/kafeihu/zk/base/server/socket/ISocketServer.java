package kafeihu.zk.base.server.socket;

import java.net.Socket;
import kafeihu.zk.base.server.IServer;

/**
 * Created by zhangkuo on 2016/11/24.
 */
public interface ISocketServer extends IServer{
    /**
     * Socket参数名称
     */
    public static final String SO_SNDBUF = "SOCKET.SO_SNDBUF";
    public static final String SO_KEEPALIVE = "SOCKET.SO_KEEPALIVE";
    public static final String SO_LINGER = "SOCKET.SO_LINGER";
    public static final String SO_TCP_NODELAY = "SOCKET.SO_TCP_NODELAY";
    public static final String SO_TIMEOUT = "SOCKET.SO_TIMEOUT";
    /**
     * ServerSocket参数
     */
    public static final String SO_RCVBUF = "SOCKET.SO_RCVBUF";
    public static final String SO_REUSEADDR = "SOCKET.SO_REUSEADDR";

    /**
     * SSLServerSocket参数
     */
    public static final String SO_NEEDCLIENTAUTH="SOCKET.SO_NEEDCLIENTAUTH";
    /**
     * 缺省的服务器端Backlog值
     */
    public static final int DefaultServerSocketBackLog = 50;
    /**
     * 缺省的Socket超时：15秒
     */
    public static final int DefaultSocketTimeoutMills = 15 * 1000;

    /**
     * 获取服务器监听端口
     *
     * @return the server port
     */
    int getLocalPort();

    /**
     * 生命周期方法：SocketServer接受(accept)客户端连接后调用
     *
     * @param socket 客户端Socket连接
     */
    void onSocketAccepted(Socket socket);
    /**
     * 生命周期方法：关闭客户端Socket连接后调用前调用
     * @param socket
     */
    void onSocketClosing(Socket socket);
    /**
     * 生命周期方法：关闭客户端Socket连接后调用
     */
    void onSocketClosed(Socket socket);
    /**
     * 设置客户端Socket连接超时，单位：毫秒。
     * @param mills
     */
    void setClientSocketTimeoutMills(int mills);
}
