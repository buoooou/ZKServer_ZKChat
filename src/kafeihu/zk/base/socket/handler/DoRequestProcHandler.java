package kafeihu.zk.base.socket.handler;

import kafeihu.zk.base.exception.BServerException;
import kafeihu.zk.base.exception.model.ErrorCodeConstants;
import kafeihu.zk.manager.ProcManager;
import kafeihu.zk.base.proc.BaseProc;
import kafeihu.zk.base.socket.model.RequestData;

import java.net.Socket;

/**
 * BServer业务请求处理器。<br>
 * Created by zhangkuo on 2016/11/25.
 */
public class DoRequestProcHandler implements ISocketRequestHandler{

    public Object handleRequest(Socket sock, Object preResult) throws Exception
    {
        if (!(preResult instanceof RequestData))
        {
            throw new BServerException(ErrorCodeConstants.CastRequestDataError);
        }

        RequestData reqData = (RequestData) preResult;

        BaseProc proc = ProcManager.getProc(reqData);

        return proc.doBaseProc(reqData);
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
        return new DoRequestProcHandler();
    }
}
