package kafeihu.zk.base.pool;

import java.util.NoSuchElementException;

/**
 * ObjectPool接口定义了资源池的公共方法。<br>
 *<b> 资源池使用举例：</b>
 *<pre> {@code
 *	Object obj = null;
 *	try {
 *		obj = pool.borrowObject();
 *   	...使用资源对象...
 *	} catch(NoSuchElement e){
 *		//资源池耗尽
 *	} catch(Exception e) {
 *	 // 系统抛出异常
 *		...判断对象是否已失效...
 *  	if(对象已失效)
 *  	{
 *  		pool.invalidateObject(obj);
 *    		// 将对象置为null，避免重复返回资源池
 *    		obj = null;
 *  	}
 *	} finally {
 *		// 确保将对象返回资源池
 *    	if(null != obj) {
 *        pool.returnObject(obj);
 *    	}
 *	}
 * }</pre>
 *
 * Created by zhangkuo on 2017/6/11.
 */
public interface ObjectPool {

    /**
     * 从对象资源池获取对象
     *
     * @throws NoSuchElementException
     *             如果连接池对象已全部耗尽，抛出该异常
     * @throws Exception
     *             其他异常，如构造新的对象时发生异常
     * @return 资源对象实例
     */
    Object borrowObject() throws NoSuchElementException, Exception;

    /**
     * 从对象资源池获取对象。
     *
     * @param timeoutMills
     *            等待时间
     *
     * @throws NoSuchElementException
     *             如果连接池对象已全部耗尽，抛出该异常
     * @throws Exception
     *             其他异常，如构造新的对象时发生异常
     * @return 资源对象实例
     */
    Object borrowObject(long timeoutMills) throws NoSuchElementException, Exception;

    /**
     * 将对象返回资源池
     *
     * @param obj
     * @throws Exception
     */
    void returnObject(Object obj) throws Exception;

    /**
     * 将指定对象置为无效。当被获取（borrow）的对象被认为无效时，调用该方法
     *
     * @param obj
     * @throws Exception
     */
    void invalidateObject(Object obj);

    /**
     * 资源池最大允许的对象数
     *
     * @return
     */
    int getMaxPoolSize();

    /**
     * 获取资源池最小保持的对象数
     *
     * @return
     */
    int getCorePoolSize();

    /**
     * 当前活跃的（被获取的）对象数
     *
     * @return
     */
    int getActiveObjectCount();

    /**
     * 当前非活跃可被获取的对象数
     *
     * @return
     */
    int getIdleObjectCount();

    /**
     * 关闭对象池，销毁池中所有对象
     *
     * @throws Exception
     */
    void close() throws Exception;

    /**
     * 设置用于构造/销毁对象的工厂类
     *
     * @param factory
     */
    void setFactory(PoolableObjectFactory factory);

    /**
     * 获取对象工厂类
     *
     * @return
     */
    PoolableObjectFactory getFactory();
}
