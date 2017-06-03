package kafeihu.zk.base.schedule;

import kafeihu.zk.base.schedule.policy.Policy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  任务调度上下文
 * Created by zhangkuo on 2017/6/1.
 */
public abstract class TaskExecutionContext {

    private Map<Object,Object> m_ValueMap= new ConcurrentHashMap<Object, Object>();

    /**
     * 获取上下文属性值。如果没有指定的属性，返回null
     *
     * @param key
     * @return
     */
    public Object getObject(Object key)
    {
        return m_ValueMap.get(key);
    }

    /**
     * 设置上下文属性值。如果已经设置过指定属性，则原来的设置被覆盖
     *
     * @param key
     * @param value
     */
    public void putObject(Object key, Object value)
    {
        m_ValueMap.put(key, value);
    }

    /**
     * 获取任务调度策略
     *
     * @return
     */
    public abstract Policy getPolicy();

    /**
     * 获取任务已成功执行的次数
     *
     * @return
     */
    public abstract int getSuccExecutedCount();

    /**
     * 获取任务执行失败的次数
     *
     * @return
     */
    public abstract int getFailedExecutedCount();

}
