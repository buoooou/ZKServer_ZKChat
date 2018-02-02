package kafeihu.zk.server.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhangkuo on 2017/5/29.
 */
public class DynamicProxy implements InvocationHandler {

    private Subject subject=null;
    public DynamicProxy(Subject subject){
        this.subject=subject;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        method.invoke(subject,args);

        return  null;
    }

}
