package kafeihu.zk.base.pool;

/**
 *
 * 对象资源池配置类
 *
 * Created by zhangkuo on 2017/6/15.
 */
public class ObjectPoolConfig {
    /**
     * 资源池最小保持的对象数.默认为2
     */
    private int m_coreSize = 2;
    /**
     * 资源池最大允许的对象数。默认为5
     */
    private int m_maxSize = 5;

    /**
     * 当对象数大于最小数时，多出的对象存活时间，单位：秒。默认为10分钟（600秒）。
     */
    private int m_keepAliveSeconds = 600;
    /**
     * 获取资源时的等待时间，单位：毫秒。默认为0
     */
    private int m_borrowWaitMills = 0;
    /**
     * 是否需要保持连接池内对象处于可用状态（对长连接对象有意义）
     */
    private boolean m_keepPoolableObjectActive = false;
    /**
     * 保持对象激活间隔：间隔多长时间取资源池对象使其处于激活状态。默认为60秒
     */
    private int m_activateObjectIntervalSeconds = 60;
    /**
     * 一次激活处理循环次数：即取几个资源对象进行激活处理。默认一次取一个
     */
    private int m_activateObjectLoopCnt = 1;

    /**
     * 获取资源池最小保持的对象数
     *
     * @return
     */
    public int getCoreSize()
    {
        return m_coreSize;
    }

    /**
     * 设置资源池最小保持的对象数
     *
     * @param size
     */
    public void setCoreSize(int size)
    {
        m_coreSize = size;
    }

    /**
     * 获取资源池最大允许的对象数
     *
     * @return
     */
    public int getMaxSize()
    {
        return m_maxSize;
    }

    /**
     * 设置资源池最大允许的对象数
     *
     * @param size
     */
    public void setMaxSize(int size)
    {
        m_maxSize = size;
    }

    /**
     * 获取多出的对象存活时间
     *
     * @return
     */
    public int getKeepAliveSeconds()
    {
        return m_keepAliveSeconds;
    }

    /**
     * 设置多出的对象存活时间
     *
     * @param aliveSeconds
     *            存活秒数
     */
    public void setKeepAliveSeconds(int aliveSeconds)
    {
        m_keepAliveSeconds = aliveSeconds;
    }

    /**
     * 返回获取资源时的等待时间
     *
     * @return
     */
    public int getBorrowWaitMills()
    {
        return m_borrowWaitMills;
    }

    /**
     * 设置获取资源时的等待时间
     *
     * @param mills
     *            等待毫秒数
     */
    public void setBorrowWaitMills(int mills)
    {
        m_borrowWaitMills = mills;
    }

    public boolean isKeepPoolableObjectActive()
    {
        return m_keepPoolableObjectActive;
    }

    public void setKeepPoolableObjectActive(boolean mKeepPoolableObjectActive)
    {
        m_keepPoolableObjectActive = mKeepPoolableObjectActive;
    }

    public int getActivateObjectIntervalSeconds()
    {
        return m_activateObjectIntervalSeconds;
    }

    public void setActivateObjectIntervalSeconds(int mActivateObjectIntervalSeconds)
    {
        m_activateObjectIntervalSeconds = mActivateObjectIntervalSeconds;
    }

    public int getActivateObjectLoopCnt()
    {
        return m_activateObjectLoopCnt;
    }

    public void setActivateObjectLoopCnt(int mActivateObjectLoopCnt)
    {
        m_activateObjectLoopCnt = mActivateObjectLoopCnt;
    }


}
