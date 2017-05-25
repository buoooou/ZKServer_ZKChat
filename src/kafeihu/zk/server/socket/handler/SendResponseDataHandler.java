package kafeihu.zk.server.socket.handler;



import kafeihu.zk.base.exception.BServerException;
import kafeihu.zk.base.exception.model.ErrorCodeConstants;
import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.manager.LoggerManager;
import kafeihu.zk.server.socket.model.ResponseData;
import kafeihu.zk.base.socket.util.SocketKit;
import kafeihu.zk.base.util.ArrayUtil;
import kafeihu.zk.base.util.MiscUtil;

import java.net.Socket;

/**
 * 返回响应结果数据处理器
 *
 * Created by zhangkuo on 2016/11/25.
 */
public class SendResponseDataHandler implements ISocketRequestHandler{
    private static final Logger m_logger = LoggerManager.getSysLogger();
    /**
     * 请求处理成功结果前缀
     */
    private final static byte[] SuccPrefix = String.valueOf('\1').getBytes();

    private static String m_logTips = SendResponseDataHandler.class.getName();

    public Object handleRequest(Socket sock, Object preResult) throws Exception
    {
        if(null == preResult)
        {
            return null;
        }
        if (!(preResult instanceof ResponseData))
        {
            throw new BServerException(ErrorCodeConstants.CastResponseDataError);

        }
        ResponseData respData = (ResponseData) preResult;
        SocketKit sockit = new SocketKit(sock);

        //构造返回数据长度
        byte[] packLen = MiscUtil.htonl(respData.getData().length + 1);
        //合并返回数据
        byte[] respBytes = ArrayUtil.joinArray(ArrayUtil.joinArray(packLen, SuccPrefix), respData
                .getData());
        try
        {
            //发送返回数据
            sockit.send(respBytes);
        }
        catch (Exception exp)
        {
            m_logger.error(m_logTips, "send response data() failed. " + exp);
            throw new BServerException(ErrorCodeConstants.SendResponse2SocketClientFailed);
        }
        return null;
    }

    /**
     * 根据请求处理器配置，构造特定的请求处理器实例
     *
     * @param xmlHandlerConfig
     * @return
     */
    public static ISocketRequestHandler buildSocketRequestHandler(String xmlHandlerConfig)
            throws Exception
    {
        return new SendResponseDataHandler();
    }
}
