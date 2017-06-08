package kafeihu.zk.bserver.service.monitor;

import java.net.Socket;

/**
 * Created by zhangkuo on 2017/6/4.
 */
public class MonitorProc {
    public static final int PACK_LEN = 4;


    public static final int PRID_LEN = 64;
    public static final char FailPrefix='\2';

    public void doProc(Socket socket, String request) {
    }

    /**
     * 发送响应数据到客户端
     *
     * @param socket
     * @param responseData
     */
    public static void sendResponse(Socket socket, char resultPrefix, String responseData)
    {

    }
}
