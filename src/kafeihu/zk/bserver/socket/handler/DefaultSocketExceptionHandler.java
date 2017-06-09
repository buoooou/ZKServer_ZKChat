package kafeihu.zk.bserver.socket.handler;

import kafeihu.zk.base.server.socket.ISocketExceptionHandler;
import kafeihu.zk.bserver.config.GlobalConfig;
import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.bserver.manager.LoggerManager;
import kafeihu.zk.base.util.SocketKit;
import kafeihu.zk.base.util.ArrayUtil;
import kafeihu.zk.base.util.MiscUtil;

import java.net.Socket;

/**
 * 缺省的Socket请求处理异常处理器
 * Created by zhangkuo on 2016/11/25.
 */
public class DefaultSocketExceptionHandler implements ISocketExceptionHandler {
    private static final Logger m_logger = LoggerManager.getSysLogger();
    /**
     * 请求处理失败结果前缀
     */
    private static char FailPrefix = '\2';

    //private static final int PACK_LEN = 4;

    public void handleException(Socket sock, Exception exp)
    {
        SocketKit sockit = new SocketKit(sock);
        try
        {
            String errMsg = exp.getMessage();
            if(null == errMsg)
            {
                errMsg = "exception message is null";
            }
            int len = errMsg.getBytes(GlobalConfig.encoding).length + 1;

            byte[] packLen = MiscUtil.htonl(len);

            StringBuilder sbData2Send = new StringBuilder();
            sbData2Send.append(FailPrefix);
            sbData2Send.append(errMsg);
            // 发送返回数据
            sockit.send(ArrayUtil.joinArray(packLen, sbData2Send.toString().getBytes(GlobalConfig.encoding)));
            // m_socketKit.send(sbData2Send.toString().getBytes());
        }
        catch (Exception e)
        {
            m_logger.error(this.getClass().getName(), "handleException() failed : " + e);
        }
    }

    /**
     * 根据请求处理器配置，构造特定的请求处理器实例
     *
     * @param xmlHandlerConfig
     * @return
     */
    public static ISocketExceptionHandler buildSocketExceptionHandler(String xmlHandlerConfig)
    {
        return new DefaultSocketExceptionHandler();
    }
}
