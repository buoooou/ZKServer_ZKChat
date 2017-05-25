package kafeihu.zk.bserver.context;

/**
 * 总应用上下文
 * Created by zhangkuo on 2016/11/24.
 */
public abstract class ApplicationContext implements IContext {

    private final String m_applicationName;

    public ApplicationContext(String mApplicationName)
    {
        super();
        m_applicationName = mApplicationName;
    }

    public ApplicationContext()
    {
        this("UniBServer");
    }

    /**
     * 获取BServer应用名称
     *
     * @return
     */
    public String getApplicationName()
    {
        return m_applicationName;
    }
}
