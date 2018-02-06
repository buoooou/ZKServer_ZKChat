package kafeihu.zk.server.console;

import kafeihu.zk.base.logging.slf4j.Slf4JUtil;
import kafeihu.zk.server.core.BserverManager;

/**
 * Server服务器启动入口
 * <p>
 * Created by zhangkuo on 2016/11/21.
 */
public class Bserver {

    public static void main(String[] args) throws Exception {

        Slf4JUtil.getConsoleLogger().info("ZKbserver start...");
        BserverManager.start();
        Slf4JUtil.getConsoleLogger().info("ZKbserver started!");

    }
}
