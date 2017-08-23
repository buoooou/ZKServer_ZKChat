package kafeihu.zk.bserver.test;

import kafeihu.zk.bserver.manager.Log4JManager;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public class TestLog {

    public static void main(String[] args) {

        Log4JManager.getSysLogger().info("测试 系统日志");
    }
}
