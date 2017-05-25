package kafeihu.zk.bserver.exception;

import kafeihu.zk.bserver.exception.model.ErrorCodeConstants;

/**
 * Created by zhangkuo on 2016/11/24.
 */
public class UndefinedModuleException extends BServerException{
    private String m_moduleName;

    public UndefinedModuleException(String modulename, String message, Throwable cause)
    {
        super(ErrorCodeConstants.ModuleUndefined, message, cause);
        m_moduleName = modulename;
    }

    public UndefinedModuleException(String modulename, String message)
    {
        super(ErrorCodeConstants.ModuleUndefined, message);
        m_moduleName = modulename;
    }

    public UndefinedModuleException(String modulename, Throwable cause)
    {
        super(ErrorCodeConstants.ModuleUndefined, cause);
        m_moduleName = modulename;
    }

    public UndefinedModuleException(String modulename)
    {
        super(ErrorCodeConstants.ModuleUndefined);
        m_moduleName = modulename;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder(super.getMessage());
        sb.append(". moduleName:").append(m_moduleName);
        return sb.toString();
    }
}
