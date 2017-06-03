package kafeihu.zk.base.schedule.policy.impl;

import kafeihu.zk.base.schedule.policy.CyclePolicy;
import kafeihu.zk.base.util.MiscUtil;

import java.util.Properties;

/**
 * 按日循环策略：每日（指定小时、分钟、秒）执行一次
 *
 * Created by zhangkuo on 2017/6/3.
 */
public class CycleDailyPolicy extends CyclePolicy {
    /**
     * 日期小时数
     */
    private int m_dayHour;
    /**
     * 分钟数
     */
    private int m_minute;
    /**
     * 秒数
     */
    private int m_second;

    /**
     * 构造函数
     *
     * @param mNextCycleDayHour
     *            下一执行循环日小时数. 取值范围0-23
     * @param mNextCycleMinute
     *            下一执行循环日小时分钟数. 取值范围0-59
     * @param mNextCycleSecond
     *            下一执行循环日小时分钟秒数. 取值范围0-59
     */
    public CycleDailyPolicy(int mNextCycleDayHour, int mNextCycleMinute, int mNextCycleSecond)
    {
        super();
        m_dayHour = mNextCycleDayHour;
        m_minute = mNextCycleMinute;
        m_second = mNextCycleSecond;
    }

    /**
     *
     * @param prop
     *            策略属性容器，属性名包括：hour,minute,second。默认为0
     */
    public CycleDailyPolicy(Properties prop) throws Exception
    {
        super();
        try
        {
            m_dayHour = Integer.valueOf(prop.getProperty("hour", "0"));
            m_minute = Integer.valueOf(prop.getProperty("minute", "0"));
            m_second = Integer.valueOf(prop.getProperty("second", "0"));
            boolean mExecFirstImmediately = prop.getProperty("execFirstImmediately", "Y")
                    .equalsIgnoreCase("Y");
            setExecFirstImmediately(mExecFirstImmediately);
            if (mExecFirstImmediately)
            {
                long firstDelay = Long.valueOf(prop.getProperty("firstDelaySeconds", "0"));
                setFirstDelaySeconds(firstDelay);
            }
        }
        catch (NumberFormatException exp)
        {
            throw new Exception(getClass().getName() + " illegal properties format:" + exp, exp);
        }
    }
    /**
     * 获取下次执行时间与当前时间间隔毫秒数
     */
    public long getNextDelayMillis()
    {
        return MiscUtil.getDuringMillisFromNextDayTime(m_dayHour, m_minute, m_second);
    }

    @Override
    public String toString()
    {
        return "CycleDailyPolicy [hour=" + m_dayHour + ", minute=" + m_minute + ", second="
                + m_second + "]";
    }


}
