package kafeihu.zk.base.schedule.policy.impl;

import kafeihu.zk.base.schedule.RepeatMode;
import kafeihu.zk.base.schedule.policy.BaseFixedSpanPolicy;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangkuo on 2017/6/3.
 */
public class FixedRatePolicy extends BaseFixedSpanPolicy {

    /**
     * 任务提交执行间隔
     */
    private long m_peroid;

    public FixedRatePolicy(long mFirstDelay, long mPeriod, TimeUnit mTimeUnit)
    {
        super(RepeatMode.FixedRate);
        if (mFirstDelay < 0)
        {
            throw new IllegalArgumentException("firstDelay value can not < 0");
        }
        if (mPeriod <= 0)
        {
            throw new IllegalArgumentException("period value must > 0");
        }

        setFirstDelay(mFirstDelay);
        setTimeUnit(mTimeUnit);

        m_peroid = mPeriod;
    }
    /**
     *
     * @param prop
     *            策略属性容器，属性名包括：firstDelay,period,默认为10，timeUnit,默认为second
     */
    public FixedRatePolicy(Properties prop) throws Exception
    {
        super(RepeatMode.FixedRate);
        try
        {
            long firstDelay = Long.valueOf(prop.getProperty("firstDelay", "10"));
            long peroid = Long.valueOf(prop.getProperty("period", "10"));
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
                peroid = peroid * 60;
            }
            else if (strTimeUnit.equalsIgnoreCase("hour"))
            {
                timeUnit = TimeUnit.SECONDS;
                firstDelay = firstDelay * 3600;
                peroid = peroid * 3600;
            }

            setFirstDelay(firstDelay);
            setTimeUnit(timeUnit);

            m_peroid = peroid;
        }
        catch (NumberFormatException exp)
        {
            throw new Exception(getClass().getName() + " illegal properties format:" + exp, exp);
        }
    }

    /**
     * 下次执行时间。返回-1表示任务是固定频率执行模式，不需下次执行时间
     */
    public long getNextDelay()
    {
        return -1;
    }

    /**
     * 返回任务提交执行间隔
     */
    public long getPeroid()
    {
        return m_peroid;
    }

    @Override
    public String toString()
    {
        return "FixedRatePolicy [peroid=" + m_peroid + ", firstDelay=" + getFirstDelay()
                + ", timeUnit=" + getTimeUnit() + "]";
    }

}
