package kafeihu.zk.base.schedule.policy;

import kafeihu.zk.base.schedule.RepeatMode;

/**
 * Created by zhangkuo on 2017/6/3.
 */
public abstract class CyclePolicy extends BasePolicy {

    /**
     * 首次任务是否立即执行
     */
    private boolean m_execFirstImmediately = true;
    /**
     * 首次任务执行延时时间
     */
    private long m_firstDelaySeconds = 0;

    public CyclePolicy()
    {
        super(RepeatMode.Cycle);
    }

    /**
     * 是否立即执行首次任务。默认为立即执行
     *
     * @return
     */
    public boolean execFirstImmediately()
    {
        return m_execFirstImmediately;
    }

    /**
     * 设置是否立即执行首次任务
     *
     * @param mExecFirstImmediately
     */
    public void setExecFirstImmediately(boolean mExecFirstImmediately)
    {
        m_execFirstImmediately = mExecFirstImmediately;
    }

    public long getFirstDelaySeconds()
    {
        return m_firstDelaySeconds;
    }

    public void setFirstDelaySeconds(long mFirstDelaySeconds)
    {
        if (mFirstDelaySeconds > 0)
        {
            m_firstDelaySeconds = mFirstDelaySeconds;
        }
    }

    /**
     * 获取下次执行时间与当前时间间隔毫秒数
     */
    public abstract long getNextDelayMillis();
}
