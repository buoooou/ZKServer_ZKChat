package kafeihu.zk.base.schedule.policy;

import kafeihu.zk.base.schedule.RepeatMode;

import java.util.concurrent.TimeUnit;

/**
 *
 * 固定间隔调度策略：<br>
 * 1.任务提交执行间隔时间固定<br>
 * 2.每一次执行终止和下一次执行开始之间间隔时间固定
 *
 * Created by zhangkuo on 2017/6/3.
 */
public abstract class BaseFixedSpanPolicy extends BasePolicy{

    public BaseFixedSpanPolicy(RepeatMode mRepeatMode)
    {
        super(mRepeatMode);
    }
    /**
     * 时间单位。默认为秒
     */
    private TimeUnit m_timeUnit = TimeUnit.SECONDS;
    /**
     * 任务初次执行延迟时间
     */
    private long m_firstDelay = 0;
    /**
     * 返回任务初次执行延迟时间：任务提交后延迟多长时间后开始执行
     *
     * @return
     */
    public long getFirstDelay()
    {
        return m_firstDelay;
    }

    /**
     * 设置任务初次执行延迟时间
     *
     * @param mFirstDelay
     *            大于或等于0，否则调用该方法无效
     */
    public void setFirstDelay(long mFirstDelay)
    {
        if (mFirstDelay >= 0)
        {
            m_firstDelay = mFirstDelay;
        }
    }

    /**
     * 设置时间单位
     *
     * @param mTimeUnit
     */
    public void setTimeUnit(TimeUnit mTimeUnit)
    {
        m_timeUnit = mTimeUnit;
    }

    /**
     * 返回时间单位
     */
    public TimeUnit getTimeUnit()
    {
        return m_timeUnit;
    }

}
