package kafeihu.zk.base.schedule;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.schedule.policy.Policy;
import kafeihu.zk.base.schedule.policy.CyclePolicy;
import kafeihu.zk.base.schedule.policy.impl.FixedDelayPolicy;
import kafeihu.zk.base.schedule.policy.impl.FixedRatePolicy;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
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
        }finally{
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
    /**
     * 调度固定频率执行任务
     *
     * @param task
     * @param policy
     * @param scheduleService
     * @throws Exception
     */
    private void scheduleFixedRateTask(Task task, FixedRatePolicy policy,
                                       ScheduledExecutorService scheduleService) throws Exception
    {
        try
        {
            // 构造任务处理工作类
            TaskWorker worker = new TaskWorker(task, policy, this);
            // 固定频率调度执行任务
            ScheduledFuture<?> future = scheduleService.scheduleAtFixedRate(worker, policy
                    .getFirstDelay(), policy.getPeroid(), policy.getTimeUnit());
            // 保持远期调度类，用于控制结束调度任务
            m_scheduleFutureMap.put(task, future);
        }
        catch (Exception exp)
        {
            throw new Exception("scheduleFixedRateTask failed:" + exp, exp);
        }
    }
    /**
     * 调度循环执行任务(首次)
     *
     * @param task
     * @param policy
     * @param scheduleService
     * @throws Exception
     */
    private void scheduleCycleTask(Task task, CyclePolicy policy,
                                   ScheduledExecutorService scheduleService) throws Exception
    {
        try
        {
            // 构造任务处理工作类
            TaskWorker worker = new TaskWorker(task, policy, this);
            long delay = 0;
            if (policy.execFirstImmediately())
            {
                delay = policy.getFirstDelaySeconds() * 1000;
            }
            else
            {
                // 计算下一次调度执行延迟时间
                delay = policy.getNextDelayMillis();
            }
            // 调度执行任务
            ScheduledFuture<?> future = scheduleService.schedule(worker, delay,
                    TimeUnit.MILLISECONDS);

            // 保持远期调度类，用于控制结束调度任务
            m_scheduleFutureMap.put(task, future);

        }
        catch (Exception exp)
        {
            throw new Exception("scheduleCycleTask failed:" + exp, exp);
        }
    }
    /**
     * 调度循环执行任务(非首次)
     *
     * @param taskWorker
     * @param task
     * @param policy
     * @throws Exception
     */
    public synchronized void scheduleCycleTaskWorker(TaskWorker taskWorker, Task task,
                                                     CyclePolicy policy)
    {
        try
        {
            ScheduledExecutorService scheduleService = m_scheduleServiceMap.get(task);
            if (null == scheduleService)
            {
                // 非首次调用，调度服务类不应为空
                throw new Exception("missing ScheduledExecutorService instance");
            }

            long delay = policy.getNextDelayMillis();
            // 调度执行任务（执行一次）
            ScheduledFuture<?> future = scheduleService.schedule(taskWorker, delay,
                    TimeUnit.MILLISECONDS);

            // 保持远期调度类，用于控制结束调度任务
            m_scheduleFutureMap.put(task, future);
        }
        catch (Exception exp)
        {
            m_logger.error(getClass().getName(), "scheduleCycleTaskWorker failed. " + task
                    + " exp:" + exp);
        }
    }

}
/**
 * 被调度任务工作类
 *
 * @author zhangkuo
 *
 */
class TaskWorker implements Runnable
{
    /**
     * 被调度任务
     */
    private Task m_task;
    /**
     * 调度策略
     */
    private Policy m_policy;
    /**
     * 关联的调度器
     */
    private Scheduler m_scheduler;
    /**
     *已成功完成执行次数
     */
    private int m_executedCount_Succ = 0;
    /**
     * 执行失败次数
     */
    private int m_executedCount_Fail = 0;

    private TaskExecutionContext m_context = new TaskExecutionContext()
    {

        @Override
        public int getSuccExecutedCount()
        {
            return m_executedCount_Succ;
        }

        @Override
        public Policy getPolicy()
        {
            return m_policy;
        }

        @Override
        public int getFailedExecutedCount()
        {
            return m_executedCount_Fail;
        }
    };

    public TaskWorker(Task mTask, Policy mpolicy, Scheduler mScheduler)
    {
        super();
        m_task = mTask;
        m_policy = mpolicy;
        m_scheduler = mScheduler;
    }

    /**
     * 执行被调度任务
     */
    private void executeTask()
    {
        try
        {
            // 任务执行前预处理
            m_task.beforeExecute(m_context);
            // 执行任务
            m_task.execute(m_context);
            // 记录成功执行次数
            m_executedCount_Succ++;
        }
        catch (Exception exp)
        {
            m_executedCount_Fail++;
            // 处理任务执行异常信息
            m_task.onExecuteException(m_context, exp);
        }
        finally
        {
            // 任务执行后处理
            m_task.afterExecute(m_context);
        }
    }

    public void run()
    {
        if (m_task.isCancelled())
        {
            // 任务被取消，结束任务调度
            m_scheduler.unscheduleTask(m_task, m_policy);
            // 任务结束通知
            m_task.onTaskFinish(m_context, TaskFinishCause.Cancelled);
            return;
        }
        // 判断任务是否已过期
        if (isTaskExpired())
        {
            // 任务已过期，结束任务调度
            m_scheduler.unscheduleTask(m_task, m_policy);
            // 任务结束通知
            m_task.onTaskFinish(m_context, TaskFinishCause.Expired);
            return;
        }

        // 执行任务
        executeTask();

        if (m_task.isCancelled())
        {
            // 任务被取消，结束任务调度
            m_scheduler.unscheduleTask(m_task, m_policy);
            // 任务结束通知
            m_task.onTaskFinish(m_context, TaskFinishCause.Cancelled);
            return;
        }
        // 判断是否已达到循环次数
        if (hasReachRepeatCount())
        {
            // 任务已过期，结束任务调度
            m_scheduler.unscheduleTask(m_task, m_policy);
            // 任务结束通知
            m_task.onTaskFinish(m_context, TaskFinishCause.ReachRepeatCount);
            return;
        }
        else
        {
            // 对于周期循环执行任务，再次调度执行
            if (m_policy.getRepeatMode() == RepeatMode.Cycle)
            {
                // m_scheduler.scheduleTask(m_task, m_policy);
                m_scheduler.scheduleCycleTaskWorker(this, m_task, (CyclePolicy) m_policy);
            }
        }

    }

    /**
     * 是否已达到循环次数限制
     *
     * @return
     */
    private boolean hasReachRepeatCount()
    {
        if (m_policy.getRepeatCount() <= 0)
        {
            // 任务一直执行，无执行次数限制
            return false;
        }
        // 判断是否已达到执行次数
        return (m_executedCount_Succ + m_executedCount_Fail) >= m_policy.getRepeatCount();
    }

    /**
     * 任务是否已过期
     *
     * @return
     */
    private boolean isTaskExpired()
    {
        if (null == m_policy.getEndDate())
        {// 无结束时间限制，永不过期
            return false;
        }
        return m_policy.getEndDate().before(new Date());
    }
}
