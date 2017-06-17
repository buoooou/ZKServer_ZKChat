package kafeihu.zk.base.pool.impl;

import kafeihu.zk.base.pool.PoolableObjectConfig;
import kafeihu.zk.base.pool.PoolableObjectFactory;

/**
 * Created by zhangkuo on 2017/6/17.
 */
public class FakeConnectionFactory implements PoolableObjectFactory {
    @Override
    public void activateObject(Object obj) throws Exception
    {
    }

    @Override
    public void destroyObject(Object obj) throws Exception
    {
        obj = null;
    }

    @Override
    public Object makeObject() throws Exception
    {
        return new Object();
    }

    @Override
    public Object makeObject(PoolableObjectConfig poolObjConfig) throws Exception
    {
        return new Object();
    }

    @Override
    public void passivateObject(Object obj) throws Exception
    {
    }

    @Override
    public boolean validateObject(Object obj)
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "FakeConnection";
    }

    @Override
    public String targetInfo()
    {
        return "FakeConnection";
    }

}
