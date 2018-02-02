package kafeihu.zk.server.test;

import kafeihu.zk.server.manager.Slf4JManager;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public class TestLog {

    public static void main(String[] args) {


        Slf4JManager.getConsoleLogger().info("测试 系统日志");

        Slf4JManager.getSysLogger().info("测试 系统日志");
        Slf4JManager.getMonitorLogger().info("测试 系统日志");
        Slf4JManager.getZKChatLogger().info("测试 系统日志");
        Slf4JManager.getConsoleLogger().info("测试 系统日志");
        Slf4JManager.getConsoleLogger().info("测试 系统日志");
    }
}
