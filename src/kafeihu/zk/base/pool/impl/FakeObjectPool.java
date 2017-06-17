package kafeihu.zk.base.pool.impl;

import kafeihu.zk.base.pool.ObjectPool;
import kafeihu.zk.base.pool.ObjectPoolConfig;
import kafeihu.zk.base.pool.PoolableObjectFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 假对象连接池：请求对象时构造一个新对象并返回，归还对象时销毁对象。<br>
 * 用于不需缓存的对象，如：Socket短连接,CTG短连接
 *
 * Created by zhangkuo on 2017/6/17.
 */
public class FakeObjectPool implements ObjectPool{
    /**
     * 对象工厂
     */
    private PoolableObjectFactory m_objectFactory;
    /**
     * 对象池配置
     */
    private ObjectPoolConfig m_poolConfig;
    /**
     * 信号量：控制活跃对象数量
     */
    private Semaphore m_avaliable;
    /**
     * 活跃（已借出对象）
     */
    private BlockingQueue<Object> m_activeObjectPool;

    /**
     * 构造函数
     *
     * @param mObjectFactory
     *            对象工厂
     * @param mPoolConfig
     *            资源池配置
     * @throws Exception
     */
    public FakeObjectPool(PoolableObjectFactory mObjectFactory, ObjectPoolConfig mPoolConfig)
            throws Exception
    {
        super();
        m_objectFactory = mObjectFactory;
        m_poolConfig = mPoolConfig;
        m_avaliable = new Semaphore(m_poolConfig.getMaxSize());
        m_activeObjectPool = new ArrayBlockingQueue<Object>(m_poolConfig.getMaxSize());
    }

    /**
     * 从对象资源池获取对象。
     *
     *
     * @throws NoSuchElementException
     *             如果连接池对象已全部耗尽，抛出该异常
     * @throws Exception
     *             其他异常，如构造新的对象时发生异常
     * @return 资源对象实例
     */
    public Object borrowObject() throws NoSuchElementException, Exception
    {
        return borrowObject(m_poolConfig.getBorrowWaitMills());
    }

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
    public Object borrowObject(long waitMills) throws NoSuchElementException, Exception
    {
        if (m_avaliable.tryAcquire(waitMills, TimeUnit.MILLISECONDS))
        {
            try
            {
                Object newObj = m_objectFactory.makeObject();
                if (m_activeObjectPool.offer(newObj))
                {
                    return newObj;
                }
                else
                {
                    throw new Exception("offer activeObject failed");
                }
            }
            catch (Throwable  t)
            {
                m_avaliable.release();
                throw new Exception(getClass().getName() + ".borrowObject(). "+t,t);
            }
        }
        else
        {
            throw new NoSuchElementException(getClass().getName() +".borrowObject() failed. meet maxSize:" + m_poolConfig.getMaxSize());
        }
    }

    /**
     * 关闭对象池，销毁池中所有对象
     *
     * @throws Exception
     */
    public void close() throws Exception
    {
        Exception exp = null;
        // 销毁连接池中的对象
        Collection<Object> col = new ArrayList<Object>();
        m_activeObjectPool.drainTo(col);
        for (Object obj : col)
        {
            try
            {
                m_avaliable.release();
                m_objectFactory.destroyObject(obj);
                obj = null;
            }
            catch (Exception e)
            {
                exp = new Exception(e);
            }
        }
        col.clear();
        if (null != exp)
        {
            throw exp;
        }
    }

    /**
     * 当前活跃的（被获取的）对象数
     *
     * @return
     */
    public int getActiveObjectCount()
    {

        return m_activeObjectPool.size();
    }

    /**
     * 当前非活跃的对象数
     *
     * @return
     */
    public int getIdleObjectCount()
    {
        return m_avaliable.availablePermits();
    }

    /**
     * 将对象返回资源池
     *
     * @param obj
     * @throws Exception
     */
    public void returnObject(Object obj) throws Exception
    {
        if(null == obj)
        {
            return;
        }
        m_activeObjectPool.remove(obj);
        try
        {
            m_objectFactory.destroyObject(obj);
            obj = null;
        }
        catch (Exception exp)
        {
        }
        finally
        {
            m_avaliable.release();
        }
    }

    /**
     * 设置用于构造/销毁对象的工厂类
     *
     * @param factory
     */
    public void setFactory(PoolableObjectFactory factory)
    {
        m_objectFactory = factory;
    }

    /**
     * 资源池最大允许的对象数
     *
     * @return
     */
    public int getMaxPoolSize()
    {
        return m_poolConfig.getMaxSize();
    }

    /**
     * 获取资源池最小保持的对象数
     *
     * @return
     */
    public int getCorePoolSize()
    {
        return m_poolConfig.getCoreSize();
    }

    /**
     * 将指定对象置为无效并返回对象池（销毁对象）。当被获取（borrow）的对象被认为无效时，调用该方法
     *
     * @param obj
     * @throws Exception
     */
    public void invalidateObject(Object obj)
    {
        if(null == obj)
        {
            return;
        }
        m_activeObjectPool.remove(obj);
        try
        {
            m_objectFactory.destroyObject(obj);
            obj = null;
        }
        catch (Exception exp)
        {
        }
        finally
        {
            m_avaliable.release();
        }
    }

    /**
     * 获取对象工厂类
     *
     * @return
     */
    public PoolableObjectFactory getFactory()
    {
        return m_objectFactory;
    }

    @Override
    public String toString()
    {
        return "[FakeObjectPool. PoolableObject:" + m_objectFactory + "]";
    }


}
