package kafeihu.zk.base.schedule.policy;

import kafeihu.zk.base.schedule.RepeatMode;

import java.util.Date;

/**
 * 调度策略基类
 *
 * Created by zhangkuo on 2017/6/3.
 */
public abstract class BasePolicy implements Policy {

    /**
     * 任务执行次数。默认为循环执行
     */
    private int m_repeatCount = 0;
    /**
     * 任务重复执行模式
     */
    private RepeatMode m_repeatMode;
    /**
     * 任务结束时间，如果为null，表示无结束时间限制
     */
    private Date m_endDate = null;

    public BasePolicy(RepeatMode mRepeatMode)
    {
        super();
        m_repeatMode = mRepeatMode;
    }
    /**
     * 返回任务结束时间，返回null表示无结束时间限制
     *
     * @return
     */
    @Override
    public Date getEndDate() {
        return m_endDate;
    }

    /**
     * 设置任务结束时间
     *
     * @param mEndDate
     *            任务结束时间<br>
     *            null，表示任务一直执行，无结束时间限制
     */
    public void setEndDate(Date mEndDate)
    {
        m_endDate = mEndDate;
    }
    /**
     * 返回任务重复执行次数：<br>
     * 重复次数<=0，表示启动任务并一直执行下去，直到达到任务结束时间<br>
     * 重复次数=1，表示只执行一次<br>
     * 重复次数>1，表示启动任务并循环执行指定的次数，直到达到任务结束时间限制
     *
     * @return
     */
    public int getRepeatCount()
    {
        return m_repeatCount;
    }

    /**
     * 设置任务重复执行次数
     *
     * @param mRepeatCount
     *            任务执行次数 <br>
     *            <=0，表示启动任务并一直执行下去，直到达到任务结束时间<br>
     *            =1，表示只执行一次<br>
     *            >1，表示启动任务并循环执行指定的次数，直到达到任务结束时间限制
     */
    public void setRepeatCount(int mRepeatCount)
    {
        m_repeatCount = mRepeatCount;
    }
    /**
     * 返回任务重复执行模式：<br>
     * 固定频率执行：任务提交执行间隔时间固定<br>
     * 固定间隔执行：每一次执行终止和下一次执行开始之间间隔时间固定<br>
     * 周期执行：每隔固定周期提交执行一次任务，如每周、每月、执行一次
     *
     * @return
     */
    public RepeatMode getRepeatMode()
    {
        return m_repeatMode;
    }

}
