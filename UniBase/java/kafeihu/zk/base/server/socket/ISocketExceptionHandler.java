package kafeihu.zk.base.server.socket;

import java.net.Socket;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public interface ISocketExceptionHandler {

    void handleException(Socket sock, Exception exp);
}
