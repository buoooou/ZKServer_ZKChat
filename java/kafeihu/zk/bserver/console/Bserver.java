package kafeihu.zk.bserver.console;

import kafeihu.zk.bserver.core.BserverManager;

/**
 * Server服务器启动入口
 * <p>
 * Created by zhangkuo on 2016/11/21.
 */
public class Bserver {

    public static void main(String[] args) throws Exception {

        System.out.println("Unibserver start...");
        BserverManager.start();
        System.out.println("Unibserver started!");


    }
}
