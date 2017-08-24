package kafeihu.zk.base.schedule.policy.impl;

import kafeihu.zk.base.schedule.RepeatMode;
import kafeihu.zk.base.schedule.policy.BaseFixedSpanPolicy;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 固定间隔循环执行策略：每一次执行终止和下一次执行开始之间间隔时间固定
 *
 * Created by zhangkuo on 2017/6/3.
 */
public class FixedDelayPolicy extends BaseFixedSpanPolicy {

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


    /**
     *
     * @param prop
     *            策略属性容器，属性名包括：firstDelay,delay,默认为10，timeUnit,默认为second
     */
    public FixedDelayPolicy(Properties prop) throws Exception
    {
        super(RepeatMode.FixedDelay);
        try
        {
            long firstDelay = Long.valueOf(prop.getProperty("firstDelay", "10"));
            long delay = Long.valueOf(prop.getProperty("delay", "10"));
            TimeUnit timeUnit = TimeUnit.SECONDS;
            String strTimeUnit = prop.getProperty("timeUnit", "second");
            if (strTimeUnit.equalsIgnoreCase("second"))
            {
                timeUnit = TimeUnit.SECONDS;
            }
            else if (strTimeUnit.equalsIgnoreCase("MilliSecond"))
            {
                timeUnit = TimeUnit.MILLISECONDS;
            }
            else if (strTimeUnit.equalsIgnoreCase("minute"))
            {
                timeUnit = TimeUnit.SECONDS;
                firstDelay = firstDelay * 60;
                delay = delay * 60;
            }
            else if (strTimeUnit.equalsIgnoreCase("hour"))
            {
                timeUnit = TimeUnit.SECONDS;
                firstDelay = firstDelay * 3600;
                delay = delay * 3600;
            }

            setFirstDelay(firstDelay);
            setTimeUnit(timeUnit);

            m_delay = delay;
        }
        catch (NumberFormatException exp)
        {
            throw new Exception(getClass().getName() + " illegal properties format:" + exp, exp);
        }
    }
    /**
     * 下次执行时间。返回-1表示任务是固定间隔执行模式，不需下次执行时间
     */
    public long getNextDelay()
    {
        return -1;
    }
    /**
     * 返回任务执行间隔
     */
    public long getDelay()
    {
        return m_delay;
    }

    @Override
    public String toString()
    {
        return "FixedDelayPolicy [delay=" + m_delay + ", firstDelay=" + getFirstDelay()
                + ", timeUnit=" + getTimeUnit() + "]";
    }
}
