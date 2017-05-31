package kafeihu.zk.base.client;

import kafeihu.zk.base.util.SocketKit;
import kafeihu.zk.base.util.ArrayUtil;
import kafeihu.zk.base.util.MiscUtil;

import java.net.Socket;

/**
 *
 *
 * UniBserver通讯客户端。<br>
 * 请求包格式：|包长(4)|业务类型(8)|PRID(64)|银行代码(4)|分行号(4)|数据(至少1)<br>
 * 返回包格式：|包长(4)|数据(至少1)
 * Created by zhangkuo on 2016/11/27.
 */
public class BServerSocketClient extends SocketClient{
    private static final int PACK_LEN = 4;
    private static final int MAX_LEN = 4194304;// 最大包长4M
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

    private String m_moduleName;
    private String m_procId;

    public BServerSocketClient()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public BServerSocketClient(Socket mSocket)
    {
        super(mSocket);
        // TODO Auto-generated constructor stub
    }

    public BServerSocketClient(String serverIp, int serverPort, int timeoutMills) throws Exception
    {
        super(serverIp, serverPort,timeoutMills);
        // TODO Auto-generated constructor stub
    }

    public void setModuleName(String mModuleName)
    {
        m_moduleName = mModuleName;
    }

    public void setProcId(String mProcId)
    {
        m_procId = mProcId;
    }

    @Override
    protected byte[] packData(byte[] data) throws Exception
    {
        if (null == m_moduleName)
        {
            throw new Exception("missing moduleName");
        }
        if (null == m_procId)
        {
            throw new Exception("missing procId");
        }
        // 构造包头
        StringBuilder sb = new StringBuilder();
        sb.append(MiscUtil.formatStr(m_moduleName, MOD_LEN, ' ', 'l'));
        sb.append(MiscUtil.formatStr(m_procId, PRID_LEN, ' ', 'l'));
        sb.append(MiscUtil.formatStr("cmb", BANK_LEN, ' ', 'l'));
        sb.append(MiscUtil.formatStr("0755", AREA_LEN, ' ', 'l'));

        // 合并包头和数据
        byte[] packData = ArrayUtil.joinArray(sb.toString().getBytes(), data);
        // 合并长度
        byte[] packLen = MiscUtil.htonl(packData.length);
        return ArrayUtil.joinArray(packLen, packData);
    }

    @Override
    protected byte[] receive(Socket socket) throws Exception
    {
        SocketKit sockit = new SocketKit(socket);
        byte[] packLen = new byte[PACK_LEN];
        int recvCnt = sockit.receive(packLen, PACK_LEN);
        if (PACK_LEN != recvCnt)
        {
            throw new Exception("receive pack len failed: packLen="+PACK_LEN+" recvLen="+recvCnt);
        }
        int dataLen = MiscUtil.ntohl(packLen);
        if (dataLen > MAX_LEN)
        {
            throw new Exception("response data length exceed MAX_LEN");
        }

        byte[] respData = new byte[dataLen];
        recvCnt = sockit.receive(respData, dataLen);
        if (dataLen != recvCnt)
        {
            throw new Exception("recvive response data failed: dataLen=" + dataLen + " recvLen=" + recvCnt);
        }
        return respData;
    }
}
