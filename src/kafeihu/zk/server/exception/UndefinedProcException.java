package kafeihu.zk.server.exception;

import kafeihu.zk.server.exception.model.ErrorCodeConstants;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class UndefinedProcException extends BServerException{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String m_procId;
    private String m_moduleName;

    public UndefinedProcException(String procid, String modulename, String message, Throwable cause)
    {
        super(ErrorCodeConstants.ProcUndefined, message, cause);
        m_procId = procid;
        m_moduleName = modulename;
    }

    public UndefinedProcException(String procid, String modulename, String message)
    {
        super(ErrorCodeConstants.ProcUndefined, message);
        m_procId = procid;
        m_moduleName = modulename;
    }

    public UndefinedProcException(String procid, String modulename, Throwable cause)
    {
        super(ErrorCodeConstants.ProcUndefined, cause);
        m_procId = procid;
        m_moduleName = modulename;
    }

    public UndefinedProcException(String procid, String modulename)
    {
        super(ErrorCodeConstants.ProcUndefined);
        m_procId = procid;
        m_moduleName = modulename;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder(super.getMessage());
        sb.append(". procId:").append(m_procId);
        sb.append(" moduleName:").append(m_moduleName);
        return sb.toString();
    }
}
