package kafeihu.zk.server.console;

import kafeihu.zk.server.core.BserverManager;
import kafeihu.zk.server.manager.Slf4JManager;

/**
 * Server服务器启动入口
 * <p>
 * Created by zhangkuo on 2016/11/21.
 */
public class Bserver {

    public static void main(String[] args) throws Exception {

        Slf4JManager.getConsoleLogger().info("ZKbserver start...");
        BserverManager.start();
        Slf4JManager.getConsoleLogger().info("ZKbserver started!");

    }
}
