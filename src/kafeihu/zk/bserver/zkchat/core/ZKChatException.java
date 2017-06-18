package kafeihu.zk.bserver.zkchat.core;

/**
 * Created by zhangkuo on 2017/6/18.
 */
public class ZKChatException extends Exception{
    private static final long serialVersionUID = -5941675431254579506L;

    /**
     * 异常错误码
     */
    private String m_errCode;
    private String m_errMsg;

    /**
     * 构造函数
     *
     * @param errCode
     *            错误码
     */
    public ZKChatException(String errCode)
    {
        super(errCode);
        m_errCode = errCode;
        // 获取错误码对应的错误信息
        m_errMsg = ChatContext.ErrorCodeMessage.getProperty(errCode, errCode);
    }

    public ZKChatException(String errCode, String message)
    {
        super(message);
        m_errCode = errCode;
        m_errMsg = ChatContext.ErrorCodeMessage.getProperty(errCode, errCode) + ":" + message;
    }


    public String getErrMessage()
    {
        return m_errMsg;
    }

    public String getErrCode()
    {
        return m_errCode;
    }
}
