package kafeihu.zk.server.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by zhangkuo on 2017/5/29.
 */
public class InvocationTest {

    public static void main(String[] args) {
        Subject sub=new RealSubject();

        InvocationHandler handler=new DynamicProxy(sub);

        Subject subject=(Subject) Proxy.newProxyInstance(handler.getClass().getClassLoader(),
                sub.getClass().getInterfaces(),handler);

        subject.send();

        subject.hello("sdf");


    }
}
