package kafeihu.zk.base.schedule;

/**
 * 任务结束原因
 * Created by zhangkuo on 2017/6/1.
 */
public enum TaskFinishCause {
    /**
     * 因任务过期而结束
     */
    Expired,
    /**
     * 因达到重复执行次数而结束
     */
    ReachRepeatCount,
    /**
     * 因主动撤销任务执行而结束
     */
    Cancelled;
}
