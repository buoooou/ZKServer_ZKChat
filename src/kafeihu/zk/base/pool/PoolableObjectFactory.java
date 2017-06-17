package kafeihu.zk.base.pool;

/**
 *
 *
 * Created by zhangkuo on 2017/6/15.
 */
public interface PoolableObjectFactory {
    /**
     * 构造对象
     *
     * @return
     * @throws Exception
     *             构造对象失败
     */
    Object makeObject() throws Exception;

    /**
     * 根据指定的资源对象配置构造对象
     *
     * @param poolObjConfig
     *            指定的资源对象配置
     * @return
     * @throws Exception
     */
    Object makeObject(PoolableObjectConfig poolObjConfig) throws Exception;

    /**
     * 销毁对象
     *
     * @param obj
     * @throws Exception
     */
    void destroyObject(Object obj) throws Exception;

    /**
     * 判断对象是否有效。如果对象已无效，
     *
     * @param obj
     * @return false 如果对象已无效
     */
    boolean validateObject(Object obj);

    /**
     * 激活对象，使对象处于可用状态。
     *
     * @param obj
     * @throws Exception
     *             激活对象失败，对象已不可用
     */
    void activateObject(Object obj) throws Exception;

    /**
     * 钝化对象，使对象处于挂起/休眠状态。
     *
     * @param obj
     * @throws Exception
     */
    void passivateObject(Object obj) throws Exception;

    /**
     * 连接池目标信息
     *
     * @return
     */
    String targetInfo();
}
