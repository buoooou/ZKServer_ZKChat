package kafeihu.zk.base.context;

/**
 * 模块上下文
 * Created by zhangkuo on 2016/11/24.
 */
public abstract class ModuleContext implements IContext{
    private final String m_moduleName;

    public ModuleContext(String mModuleName)
    {
        super();
        m_moduleName = mModuleName;
    }

    public String getModuleName()
    {
        return m_moduleName;
    }
}
