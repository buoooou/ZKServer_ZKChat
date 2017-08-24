package kafeihu.zk.base.schedule.policy.impl;

import kafeihu.zk.base.schedule.policy.CyclePolicy;
import kafeihu.zk.base.util.MiscUtil;

import java.util.Calendar;
import java.util.Properties;

/**
 *
 * 按周循环策略：每周(指定某一天、小时、分钟、秒)执行一次
 *
 * Created by zhangkuo on 2017/6/3.
 */
public class CycleWeeklyPolicy extends CyclePolicy {

    /**
     * 周天值：Calendar.SUNDAY,Calendar.Monday,....Calendar.Saturday
     */
    private int m_weekDay;
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
     * @param mNextCycleWeekDay
     *            下一执行循环周日数.
     *            取值范围Calendar.SUNDAY,Calendar.MONDAY,...,Calendar.SATURDAY
     * @param mNextCycleDayHour
     *            下一执行循环日小时数. 取值范围0-23
     * @param mNextCycleMinute
     *            下一执行循环日小时分钟数. 取值范围0-59
     * @param mNextCycleSecond
     *            下一执行循环日小时分钟秒数. 取值范围0-59
     */
    public CycleWeeklyPolicy(int mNextCycleWeekDay, int mNextCycleDayHour, int mNextCycleMinute,
                             int mNextCycleSecond)
    {
        super();
        m_weekDay = mNextCycleWeekDay;
        m_dayHour = mNextCycleDayHour;
        m_minute = mNextCycleMinute;
        m_second = mNextCycleSecond;
    }

    /**
     *
     * @param prop
     *            策略属性容器，属性名包括：weekday，默认为monday。hour,minute,second，默认为0
     */
    public CycleWeeklyPolicy(Properties prop) throws Exception
    {
        super();
        try
        {
            String weekday = prop.getProperty("weekday", "monday");
            m_weekDay = getWeekday(weekday);
            m_dayHour = Integer.valueOf(prop.getProperty("hour", "0"));
            m_minute = Integer.valueOf(prop.getProperty("minute", "0"));
            m_second = Integer.valueOf(prop.getProperty("second", "0"));
            boolean mExecFirstImmediately = prop.getProperty("execFirstImmediately", "Y").equalsIgnoreCase("Y");
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
    private int getWeekday(String weekday) throws Exception
    {
        int weekdayValue = -1;
        try
        {
            weekdayValue = Integer.valueOf(weekday);
            if (weekdayValue < 0)
            {
                throw new Exception(" weekday can not <0");
            }
            if (weekdayValue > 6)
            {
                throw new Exception(" weekday can not >6");
            }
        }
        catch (NumberFormatException nfe)
        {
        }
        if (weekdayValue >= 0)
        {
            return weekdayValue + 1;
        }

        if (weekday.equalsIgnoreCase("SUNDAY"))
        {
            return Calendar.SUNDAY;
        }
        if (weekday.equalsIgnoreCase("MONDAY"))
        {
            return Calendar.MONDAY;
        }
        if (weekday.equalsIgnoreCase("TUESDAY"))
        {
            return Calendar.TUESDAY;
        }
        if (weekday.equalsIgnoreCase("WEDNESDAY"))
        {
            return Calendar.WEDNESDAY;
        }
        if (weekday.equalsIgnoreCase("THURSDAY"))
        {
            return Calendar.THURSDAY;
        }
        if (weekday.equalsIgnoreCase("FRIDAY"))
        {
            return Calendar.FRIDAY;
        }
        if (weekday.equalsIgnoreCase("SATURDAY"))
        {
            return Calendar.SATURDAY;
        }
        return Calendar.MONDAY;
    }
    private String getWeekday(int weekday)
    {
        if (Calendar.SUNDAY == weekday)
        {
            return "SUNDAY";
        }
        if (Calendar.MONDAY == weekday)
        {
            return "MONDAY";
        }
        if (Calendar.TUESDAY == weekday)
        {
            return "TUESDAY";
        }
        if (Calendar.WEDNESDAY == weekday)
        {
            return "WEDNESDAY";
        }
        if (Calendar.THURSDAY == weekday)
        {
            return "THURSDAY";
        }
        if (Calendar.FRIDAY == weekday)
        {
            return "FRIDAY";
        }
        if (Calendar.SATURDAY == weekday)
        {
            return "SATURDAY";
        }
        return Integer.toString(weekday);
    }
    /**
     * 获取下次执行时间与当前时间间隔毫秒数
     */
    public long getNextDelayMillis()
    {
        return MiscUtil
                .getDuringMillisFromNextWeekDayTime(m_weekDay, m_dayHour, m_minute, m_second);
    }

    @Override
    public String toString()
    {
        return "CycleWeeklyPolicy [weekDay=" + getWeekday(m_weekDay) + ", hour=" + m_dayHour
                + ", minute=" + m_minute + ", second=" + m_second + "]";
    }
}
