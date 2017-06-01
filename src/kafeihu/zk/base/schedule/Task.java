package kafeihu.zk.base.schedule;

import java.util.Properties;

/**
 * 要调度的任务
 *
 * Created by zhangkuo on 2017/6/1.
 */
public abstract class Task {

    /**
     * 任务名称。
     */
    private String name;

    /**
     * 任务是否已被Cancelled
     */
    private volatile boolean cancelled=false;

    /**
     * 任务附加属性
     */
    private Properties prop;


    public Task(String name){
        super();
        this.name = name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * 设置任务附加属性。由框架自动调用
     *
     * @param
     */
    public void setParam(Properties extProps)
    {
        prop = extProps;
    }

    /**
     * 获取任务附加属性
     *
     * @param strKey
     * @param strDefaultValue
     *            缺省值
     * @return
     */
    public String getParam(String strKey, String strDefaultValue)
    {
        return prop.getProperty(strKey, strDefaultValue);
    }
    /**
     * 任务初始化。任务调度器调度任务前执行。子类可实现该方法进行任务初始化
     */
    public void initialize() throws Exception
    {
    }
    /**
     * 任务执行前调用。子类可覆盖实现该方法
     *
     * @param context
     *            任务执行上下文
     */
    public void beforeExecute(TaskExecutionContext context)
    {
    }

    /**
     * 任务执行接口：调用该接口完成具体的任务
     *
     * @param context
     *            任务执行上下文
     * @throws Exception
     */
    public abstract void execute(TaskExecutionContext context) throws Exception;

    /**
     * 任务执行后调用。子类可覆盖实现该方法
     *
     * @param context
     *            任务执行上下文
     */
    public void afterExecute(TaskExecutionContext context)
    {
    }

    /**
     * 如果execute方法抛出异常，调用该方法
     *
     * @param context
     *            任务执行上下文
     * @param exp
     */
    public void onExecuteException(TaskExecutionContext context, Exception exp)
    {
    }

    /**
     * 任务结束调度时，调用该方法
     *
     * @param context
     *            任务执行上下文
     * @param cause
     */
    public void onTaskFinish(TaskExecutionContext context, TaskFinishCause cause)
    {
    }

    /**
     * 取消任务调度。对需重复执行的任务，不再继续调度执行
     */
    public void cancel()
    {
        cancelled = true;
    }
    /**
     * 任务是否被主动cancel
     *
     * @return
     */
    public boolean isCancelled()
    {
        return cancelled;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "Task [name=" + name + "]";
    }

}
