package kafeihu.zk.base.schedule.policy.impl;

import kafeihu.zk.base.schedule.RepeatMode;
import kafeihu.zk.base.schedule.policy.BasePolicy;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 固定间隔循环执行策略：每一次执行终止和下一次执行开始之间间隔时间固定
 *
 * Created by zhangkuo on 2017/6/3.
 */
public class FixedDelayPolicy extends BasePolicy {

    /**
     * 任务执行完成间隔
     */
    private long m_delay;

    /**
     *
     * @param mFirstDelay
     *            任务首次执行延迟时间，0表示立即执行
     * @param mDelay
     *            任务执行完成间隔，必须大于0
     * @param mTimeUnit
     *            时间单位
     */
    public FixedDelayPolicy(long mFirstDelay, long mDelay, TimeUnit mTimeUnit)
    {
        super(RepeatMode.FixedDelay);
        if (mFirstDelay < 0)
        {
            throw new IllegalArgumentException("firstDelay value can not < 0");
        }
        if (mDelay <= 0)
        {
            throw new IllegalArgumentException("delay value must > 0");
        }

        setFirstDelay(mFirstDelay);
        setTimeUnit(mTimeUnit);

        m_delay = mDelay;
    }

    @Override
    public int getRepeatCount() {
        return 0;
    }

    @Override
    public RepeatMode getRepeatMode() {
        return null;
    }

    @Override
    public Date getEndDate() {
        return null;
    }
}
