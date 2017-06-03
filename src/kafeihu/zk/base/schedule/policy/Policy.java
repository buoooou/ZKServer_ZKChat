package kafeihu.zk.base.schedule.policy;

import kafeihu.zk.base.schedule.RepeatMode;

import java.util.Date;

/**
 * 任务调度策略接口
 * Created by zhangkuo on 2017/6/3.
 */
public interface Policy {
    /**
     * 返回任务重复执行次数：<br>
     * 重复次数<=0，表示启动任务并一直执行下去，直到达到任务结束时间<br>
     * 重复次数=1，表示只执行一次<br>
     * 重复次数>1，表示启动任务并循环执行指定的次数，直到达到任务结束时间限制
     *
     * @return
     */
    int getRepeatCount();

    /**
     * 返回任务重复执行模式：<br>
     * 固定频率执行：任务提交执行间隔时间固定<br>
     * 固定间隔执行：任务执行完成间隔时间固定<br>
     * 周期执行：每隔固定周期提交执行一次任务，如每周、每月、执行一次
     *
     * @return
     */
    RepeatMode getRepeatMode();



    /**
     * 返回任务结束时间，返回null表示无结束时间限制
     *
     * @return
     */
    Date getEndDate();
}
