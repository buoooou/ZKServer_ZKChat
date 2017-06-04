package kafeihu.zk.bserver.service.schedule;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.schedule.Task;
import kafeihu.zk.bserver.context.ModuleContext;
import kafeihu.zk.bserver.manager.ContextManager;
import kafeihu.zk.bserver.manager.LoggerManager;

/**
 * Created by zhangkuo on 2017/6/3.
 */
public abstract class ModuleTask extends Task{
    private final String m_moduleName;

    public ModuleTask(String name, String moduleName)
    {
        super(name);
        m_moduleName = moduleName;
    }

    public String getModuleName()
    {
        return m_moduleName;
    }
    /**
     * 获取模块日志处理类
     *
     * @return
     */
    public Logger getModuleLogger()
    {
        return LoggerManager.getModuleLogger(m_moduleName);
    }

    /**
     * 获取模块上下文
     *
     * @return
     */
    public ModuleContext getModuleContext()
    {
        try
        {
            return ContextManager.getModuleContext(m_moduleName);
        }
        catch (Exception exp)
        {
            // should not reach here
            throw new RuntimeException(exp.getMessage(), exp);
        }
    }
    @Override
    public String toString()
    {
        return "ModuleTask [name=" + getName() + " , moduleName=" + m_moduleName + "]";
    }

}
