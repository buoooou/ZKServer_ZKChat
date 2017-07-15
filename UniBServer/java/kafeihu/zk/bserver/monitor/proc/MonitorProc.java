package kafeihu.zk.bserver.monitor.proc;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.util.ArrayUtil;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.SocketKit;
import kafeihu.zk.bserver.core.config.GlobalConfig;
import kafeihu.zk.bserver.manager.LoggerManager;

import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * Created by zhangkuo on 2017/6/9.
 *
 */
public abstract class MonitorProc {

    public static final int SOCKET_TIME_OUT = 15000;
    /**
     * 启动服务
     */
    public static final String StartService = "start";
    /**
     * 停止服务
     */
    public static final String StopService = "stop";
    /**
     * 更改服务参数
     */
    public static final String ModifyServiceParam = "modifyparam";

    /**
     * 表示请求数据长度字节数
     */
    public static final int PACK_LEN = 4;
    /**
     * PRID长度
     */
    public static final int PRID_LEN = 64;
    /**
     * 处理成功前缀
     */
    public static final char SuccPrefix='\1';
    /**
     * 处理失败前缀
     */
    public static final char FailPrefix='\2';

    private final static Logger m_logger = LoggerManager.getSysLogger();

    private String id = "";

    /**
     * 设置附加参数
     *
     * @param param
     */
    public void setParams(String param)
    {
    }

    public void doProc(Socket socket, String request) throws Exception{
        socket.setSoTimeout(SOCKET_TIME_OUT);
        String response = procRequest(request);
        logMonitorResult(socket.getInetAddress(), response);
        sendResponse(socket, SuccPrefix, response);

    }

    /**
     * 处理监控请求
     *
     * @param request
     * @return
     * @throws Exception
     */
    protected abstract String procRequest(String request) throws Exception;


    protected void logMonitorResult(InetAddress monitor, String result)
    {
        if (null != monitor)
        {
            m_logger.info(id,result+". reqClient:"+monitor.getHostAddress());
        }
    }
    /**
     * 发送响应数据到客户端
     *
     * @param socket
     * @param responseData
     */
    public static void sendResponse(Socket socket, char resultPrefix, String responseData){
        if (MiscUtil.isEmpty(responseData))
        {
            return;
        }

        try
        {
            SocketKit sockit = new SocketKit(socket);
            responseData = resultPrefix + responseData;
            byte[] responseDataByte = responseData.getBytes(GlobalConfig.encoding);
            int len = responseDataByte.length;
            sockit.send(ArrayUtil.joinArray(MiscUtil.htonl(len), responseDataByte));

        }
        catch (Exception exp)
        {
            m_logger.error(MonitorProc.class.getName(), "sendResponse failed : " + exp);
        }

    }
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
