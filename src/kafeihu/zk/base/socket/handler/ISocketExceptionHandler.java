package kafeihu.zk.base.socket.handler;

import java.net.Socket;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public interface ISocketExceptionHandler {

    void handleException(Socket sock, Exception exp);
}
