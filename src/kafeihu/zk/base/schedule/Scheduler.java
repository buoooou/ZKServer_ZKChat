package kafeihu.zk.base.schedule;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.schedule.policy.Policy;
import kafeihu.zk.base.schedule.policy.impl.CyclePolicy;
import kafeihu.zk.base.schedule.policy.impl.FixedDelayPolicy;
import kafeihu.zk.base.schedule.policy.impl.FixedRatePolicy;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 任务调度器
 *
 * Created by zhangkuo on 2017/6/1.
 */
public class Scheduler  {

    private Map<Task,ScheduledExecutorService> m_scheduleServiceMap=new ConcurrentHashMap<Task,ScheduledExecutorService>();

    private Map<Task,ScheduledFuture<?>> m_scheduleFutureMap=new ConcurrentHashMap<Task,ScheduledFuture<?>>();

    private Logger m_logger = Logger.getConsoleLogger();
    /**
     * 默认的任务调度器实例
     */
    private static Scheduler m_defaultScheduler = null;

    private Scheduler()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * 获取默认的任务调度器实例
     *
     * @return
     */
    public static Scheduler getDefaultScheduler(){
        if(m_defaultScheduler==null)
            m_defaultScheduler=SchedulerUtil.m_defaultScheduler;
        return m_defaultScheduler;
    }
    /***
     * 单例，懒加载内部类
     */
    private static class SchedulerUtil{
        /**
         * 默认的任务调度器实例
         */
        private final static Scheduler m_defaultScheduler = new Scheduler();

    }
    /**
     * 获取关联日志工具类
     *
     * @return
     */
    public Logger getLogger()
    {
        return m_logger;
    }

    /**
     * 设置关联日志工具类。默认的日志输出到控制台
     *
     * @param mLogger
     */
    public void setLogger(Logger mLogger)
    {
        m_logger = mLogger;
    }

    /**
     * 终止所有调度任务
     *
     * @return
     */
    public synchronized void unscheduleTask() {
        Collection<ScheduledFuture<?>> colFuture = m_scheduleFutureMap.values();
        for (ScheduledFuture<?> scheFuture : colFuture)
        {
            if (null != scheFuture)
            {
                scheFuture.cancel(false);
            }
        }
        Collection<ScheduledExecutorService> colService = m_scheduleServiceMap.values();
        for (ScheduledExecutorService scheService : colService)
        {
            if (null != scheService)
            {
                try
                {
                    scheService.shutdown();
                }
                catch (Exception exp)
                {
                    m_logger.error(getClass().getName(), "unscheduleTask failed.  exp:" + exp);
                }
            }
        }
    }


    /***
     * 终止特定任务
     * @param task
     * @param policy
     * @return
     */
    public synchronized boolean unscheduleTask(Task task, Policy policy){
        ScheduledFuture<?> scheFuture;
        ScheduledExecutorService scheService;
        try {
           scheFuture=m_scheduleFutureMap.remove(task);
            if (null != scheFuture)
            {
                scheFuture.cancel(false);
            }

            scheService = m_scheduleServiceMap.remove(task);
            if (null != scheService)
            {
                scheService.shutdown();
            }
            return true;

        }catch (Exception exp){
            m_logger.error(getClass().getName(), "unscheduleTask failed. " + task + " exp:" + exp);
        }final{
            scheFuture = null;
            scheService = null;
        }
        return false;
    }
    /**
     * 用特定策略调度指定任务
     *
     * @param task
     *            要调度执行的任务
     * @param policy
     *            任务调度策略
     */
    public synchronized boolean scheduleTask(Task task, Policy policy)
    {
        try{
            task.initialize();
            ScheduledExecutorService scheduledService=m_scheduleServiceMap.get(task);
            if(scheduledService==null){
                scheduledService= Executors.newScheduledThreadPool(1);
                m_scheduleServiceMap.put(task, scheduledService);
            }
            if (policy.getRepeatMode() == RepeatMode.FixedDelay)
            {
                scheduleFixedDelayTask(task, (FixedDelayPolicy) policy, scheduledService);
            }
            else if (policy.getRepeatMode() == RepeatMode.FixedRate)
            {
                scheduleFixedRateTask(task, (FixedRatePolicy) policy, scheduledService);
            }
            else if (policy.getRepeatMode() == RepeatMode.Cycle)
            {
                scheduleCycleTask(task, (CyclePolicy) policy, scheduledService);
            }
            else
            {
                throw new Exception("illegal RepeatMode:" + policy.getRepeatMode());
            }
            return true;


        }catch (Exception exp){
            m_logger.error(getClass().getName(), "scheduleTask failed. " + task + " exp:" + exp);
        }
        return false;
    }

    /**
     * 调度固定间隔执行任务
     *
     * @param task
     * @param policy
     * @throws Exception
     */
    private void scheduleFixedDelayTask(Task task, FixedDelayPolicy policy,
                                        ScheduledExecutorService scheduleService) throws Exception
    {
        try
        {
            // 构造任务处理工作类
            TaskWorker worker = new TaskWorker(task, policy, this);
            // 固定间隔调度执行任务
            ScheduledFuture<?> future = scheduleService.scheduleWithFixedDelay(worker, policy
                    .getFirstDelay(), policy.getDelay(), policy.getTimeUnit());
            // 保持远期调度类，用于控制结束调度任务
            m_scheduleFutureMap.put(task, future);
        }
        catch (Exception exp)
        {
            throw new Exception("scheduleFixedDelayTask failed:" + exp, exp);
        }
    }

}
