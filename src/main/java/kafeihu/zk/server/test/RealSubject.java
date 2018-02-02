package kafeihu.zk.server.test;

/**
 * Created by zhangkuo on 2017/5/29.
 */
public class RealSubject implements Subject {
    @Override
    public void send() {
        System.out.println("zhangkuo real send");
    }

    @Override
    public void hello(String str) {
        System.out.println("zhangkuo hello:"+str);
    }
}
