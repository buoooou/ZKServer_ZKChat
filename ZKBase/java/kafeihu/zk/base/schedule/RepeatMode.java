package kafeihu.zk.base.schedule;

/**
 * 任务重复执行模式
 *
 * Created by zhangkuo on 2017/6/3.
 */
public enum RepeatMode {

    /**
     * 固定间隔循环执行：任务提交执行间隔时间固定
     */
    FixedRate,
    /**
     * 固定间隔循环执行：每一次执行终止和下一次执行开始之间间隔时间固定
     */
    FixedDelay,
    /**
     * 固定周期循环执行：每隔固定的时间执行一次，如每周、每月、每日等
     */
    Cycle;
}
