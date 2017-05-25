package kafeihu.zk.bserver.socket.handler;

import kafeihu.zk.bserver.exception.EarlyFinishException;

import java.net.Socket;

/**
 *
 * Socket请求处理器：负责处理客户端Socket请求。一个Socket服务器需关联一个或多个请求处理器<br>
 * 具体的请求处理器实现必须为线程安全的。
 * Created by zhangkuo on 2016/11/25.
 */
public interface ISocketRequestHandler {
    /**
     * 处理Socket请求
     *
     * @param sock
     *            客户端Socket连接
     * @param preResult
     *            前一个请求处理器的返回结果
     * @return
     * @throws EarlyFinishException
     *             如果要提前终止整个请求处理过程，可抛出该异常
     * @throws Exception
     *             处理过程中抛出异常，由关联的异常处理器进行处理
     */
    Object handleRequest(Socket sock, Object preResult) throws EarlyFinishException, Exception;

}
