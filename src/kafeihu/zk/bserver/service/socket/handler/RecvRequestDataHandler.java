package kafeihu.zk.bserver.service.socket.handler;

import kafeihu.zk.base.server.socket.ISocketRequestHandler;
import kafeihu.zk.bserver.config.GlobalConfig;
import kafeihu.zk.bserver.exception.BServerException;
import kafeihu.zk.bserver.exception.EarlyFinishException;
import kafeihu.zk.bserver.exception.model.ErrorCodeConstants;
import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.bserver.manager.LoggerManager;
import kafeihu.zk.bserver.proc.RequestData;
import kafeihu.zk.base.util.SocketKit;
import kafeihu.zk.base.util.MiscUtil;

import java.net.Socket;

/**
 * 接收请求数据处理器<br>
 *
 * 渠道到BServer的通讯握手协议：<br>
 *
 * 请求包格式：|包长(4)|业务类型(8)|PRID(64)|银行代码(4)|分行号(4)|数据(至少1)<br>
 * 返回包格式：|包长(4)|数据(至少1)<br>
 *
 * 注意：该类可能被多个线程同时调用，必须满足线程安全要求。
 * Created by zhangkuo on 2016/11/25.
 */
public class RecvRequestDataHandler implements ISocketRequestHandler {
    private static final Logger m_logger = LoggerManager.getSysLogger();
    /**
     * 数据包长度
     */
    private static final int PACK_LEN = 4;
    /**
     * 模块名称（业务类型）长度
     */
    private static final int MOD_LEN = 8;
    /**
     * Prid长度
     */
    private static final int PRID_LEN = 64;
    /**
     * 银行代码长度
     */
    private static final int BANK_LEN = 4;
    /**
     * 分行代码长度
     */
    private static final int AREA_LEN = 4;

    /**
     * 最大请求包长:4M
     */
    private static final int MAX_LEN = 1024*1024*4;

    /**
     * 最小包长
     */
    private static final int MIN_LEN = MOD_LEN + PRID_LEN + BANK_LEN + AREA_LEN;

    /**
     * 是否支持故障检测
     */
    private final boolean m_supportFaultDetect;
    /**
     * 故障检测类
     */
//    private FaultDetector[] m_faultDetectors;

    private static String m_logTips = RecvRequestDataHandler.class.getName();

    public RecvRequestDataHandler()
    {
        m_supportFaultDetect = false;
    }

//    public RecvRequestDataHandler(FaultDetector[] mFaultDetector)
//    {
//        if ((null != mFaultDetector) && (mFaultDetector.length > 0))
//        {
//            m_supportFaultDetect = true;
//            m_faultDetectors = mFaultDetector;
//        }
//        else
//        {
//            m_supportFaultDetect = false;
//        }
//    }

    public Object handleRequest(Socket sock, Object preResult) throws Exception
    {
        SocketKit sockit = new SocketKit(sock);

        // String requestHeader = null;
        try
        {
            byte[] packLenBuffer = new byte[PACK_LEN];
            // 接收包长度数据信息
            int totalRead = sockit.receive(packLenBuffer, PACK_LEN);
            if (totalRead <= 0)
            {
                return null;
            }
            if (totalRead != PACK_LEN)
            {
                throw new Exception("Receive request data header failed");
            }

            // 获取数据包长度。如果要支持服务（ACE，what'up等）探测包，在此处增加相关处理
            int packLen = MiscUtil.ntohl(packLenBuffer);

            if (packLen < MIN_LEN || packLen > MAX_LEN)
            {
                //报文长度超长时，可能是探测报文
                if(m_supportFaultDetect)
                {
                    // 请求包头
                    String requestHeader = new String(packLenBuffer);
//                    //成功进行了故障检测
//                    if(doFaultDetect(requestHeader, sockit))
//                    {
//                        throw new EarlyFinishException();
//                    }
                }
                throw new Exception("Illegal request data length:" + packLen);
            }
            byte[] requestData = new byte[packLen];
            // 接收具体数据包数据
            totalRead = sockit.receive(requestData, packLen);
            if (totalRead != packLen)
            {
                throw new Exception("Receive request data failed");
            }

            // 解析模块名称
            byte[] moduleName = new byte[MOD_LEN];
            System.arraycopy(requestData, 0, moduleName, 0, MOD_LEN);

            // 解析PRID
            byte[] procId = new byte[PRID_LEN];
            System.arraycopy(requestData, MOD_LEN, procId, 0, PRID_LEN);

            // 解析请求数据
            int dataLen = packLen - MIN_LEN;
            byte[] data = new byte[dataLen];
            System.arraycopy(requestData, MIN_LEN, data, 0, dataLen);

            RequestData requestObj = new RequestData();
            requestObj.setModuleName(new String(moduleName, GlobalConfig.encoding));
            requestObj.setProcId(new String(procId, GlobalConfig.encoding));
            requestObj.setData(data);
            return requestObj;
        }
        catch(EarlyFinishException exp)
        {
            //抛出该异常，结束整个处理流程
            throw exp;
        }
        catch (Exception exp)
        {
            m_logger.error(m_logTips, "receiveRequestData() failed. " + exp);
            throw new BServerException(ErrorCodeConstants.ReceiveSocketClientRequestFailed);
        }

    }


//    /**
//     * 执行故障检测
//     *
//     * @param detectData
//     * @param sockit
//     * @return 如果成功执行了故障检测，返回true，否则(当前数据不是故障检测数据)，返回false
//     */
//    private boolean doFaultDetect(String detectData, SocketKit sockit)
//    {
//        for (FaultDetector detector : m_faultDetectors)
//        {
//            if (detector.getDetectData().equals(detectData))
//            {
//                if (detector.isFaulty())
//                {
//                    sendFaultDetectResult(detector.getFailResponse(), sockit);
//                }
//                else
//                {
//                    sendFaultDetectResult(detector.getOkResponse(), sockit);
//                }
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 返回故障检测结果
     *
     * @param resp
     * @param sockit
     */
    private void sendFaultDetectResult(String resp, SocketKit sockit)
    {
        try
        {
            sockit.send(resp.getBytes(GlobalConfig.encoding));
        }
        catch (Exception exp)
        {
            m_logger.error(m_logTips, "sendFaultDetectResult() failed. " + exp);
        }
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
//        List<String> listFaultDetect = XmlUtil.getAllXmlElements("faultDetector", xmlHandlerConfig);
//        List<FaultDetector> listDetector = new ArrayList<FaultDetector>();
//        for (String xmlFaultDetect : listFaultDetect)
//        {
//            FaultDetector detector = FaultDetector.createFaultDetector(xmlFaultDetect);
//            if(detector.getDetectData().length() != PACK_LEN)
//            {
//                throw new Exception("illegal detectData : length must be " + PACK_LEN);
//            }
//            listDetector.add(detector);
//        }
//        return new RecvRequestDataHandler(listDetector.toArray(new FaultDetector[] {}));
        return new RecvRequestDataHandler();
    }

}
