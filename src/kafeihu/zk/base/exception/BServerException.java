package kafeihu.zk.base.exception;

import kafeihu.zk.base.util.ResourceUtil;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zhangkuo on 2016/11/24.
 */
public class BServerException extends Exception{
    private static final long serialVersionUID = 1L;
    private final static String Config_File_Name = "errorcode.xml";
    private static Properties ErrorCodeMessage = new Properties();

    static
    {
        InputStream is = null;
        try
        {
            if (ResourceUtil.isSysDataResourceExists(Config_File_Name))
            {
                is = ResourceUtil.getSysDataResourceAsStream(Config_File_Name);
                ErrorCodeMessage.loadFromXML(is);
            }
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(BServerException.class.getName()
                    + " load error code definition failed. " + exp);
        }
        finally
        {
            try
            {
                if (null != is)
                {
                    is.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * 获取错误码对应的错误信息
     *
     * @param errcode
     * @return
     */
    public static String getErrorCodeMessage(String errcode)
    {
        return ErrorCodeMessage.getProperty(errcode, errcode);
    }

    /**
     * 异常错误码
     */
    private String m_errCode;

    /**
     * 构造函数
     *
     * @param errCode
     *            错误码
     */
    public BServerException(String errCode)
    {
        super(getErrorCodeMessage(errCode));
        m_errCode = errCode;
    }

    public BServerException(String errCode, String message, Throwable cause)
    {
        super(message, cause);
        m_errCode = errCode;
    }

    public BServerException(String errCode, String message)
    {
        super(message);
        m_errCode = errCode;
    }

    public BServerException(String errCode, Throwable cause)
    {
        super(getErrorCodeMessage(errCode), cause);
        m_errCode = errCode;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder(getErrCode());
        sb.append(" ");
        sb.append(super.getMessage());
        Throwable cause = getCause();
        if (null != cause)
        {
            sb.append(" Cause:" + getCause());
        }
        return sb.toString();
    }

    public String getErrCode()
    {
        return m_errCode;
    }
}
