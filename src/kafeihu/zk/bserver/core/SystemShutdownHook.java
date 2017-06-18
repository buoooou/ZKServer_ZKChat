package kafeihu.zk.bserver.core;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.bserver.manager.*;

/**
 * 虚拟机关闭挂钩：系统（虚拟机）退出（正常退出或用户中断ctrl+c）时运行<br>
 * 在挂钩中关闭相关系统资源、记录日志等
 *
 * Created by zhangkuo on 2017/6/18.
 */
public class SystemShutdownHook extends Thread{
    private static final Logger m_logger = LoggerManager.getSysLogger();

    @Override
    public void run()
    {
        doShutdownProc();
    }

    /**
     * 系统关闭处理：停止服务、回收资源等。<br>
     * 子类可覆盖该方法实现特定的处理
     */
    protected void doShutdownProc()
    {
        // 停止BServer服务
        UniBserverManager.stop();

        //停止资源池
        ObjectPoolManager.stopObjectPool();

        //停在异步调度管理器
        ExecutorManager.stopExecutor();

        //停止数据库连接池
        DBConnectionPoolManager.stopDBConnectionPool();

        // 写系统退出日志
        m_logger.info(ContextManager.getApplicationContext().getApplicationName(), "System exit");
        m_logger.flush();
        m_logger.close();
    }
}
