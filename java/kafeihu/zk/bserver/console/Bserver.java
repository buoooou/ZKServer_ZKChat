package kafeihu.zk.bserver.console;

import kafeihu.zk.bserver.core.UniBserverManager;

/**
 * Server服务器启动入口
 *
 * Created by zhangkuo on 2016/11/21.
 */
public class Bserver {

    public static void main(String[] args) throws Exception{

        System.out.println("Unibserver start...");
        UniBserverManager.start();
        System.out.println("Unibserver started!");


    }
}
