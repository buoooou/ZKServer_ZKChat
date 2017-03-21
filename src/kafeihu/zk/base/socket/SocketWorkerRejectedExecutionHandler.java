package kafeihu.zk.base.socket;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 被拒绝Socket请求处理任务执行程序。
 * Created by zhangkuo on 2016/11/25.
 */
public abstract class SocketWorkerRejectedExecutionHandler implements RejectedExecutionHandler {
    private AtomicLong m_rejectedCounter = new AtomicLong(0);

    /**
     * 当线程池不能接受某个任务时，由ThreadPoolExecutor调用的方法<br>
     * 因为超出其界限而没有更多可用的线程或队列槽时，或者关闭 Executor 时就可能发生这种情况。
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
    {
        // 获取Socket工作者实例
        SocketWorker socketWorker = (SocketWorker) r;

        // 处理拒绝执行操作
        handleRejectedExecution(socketWorker, executor);

        // 关闭客户端Socket连接（对于Thread-per-connection模式须如此处理）
        socketWorker.closeSocket();

        //拒绝次数计数器加一
        m_rejectedCounter.incrementAndGet();
    }

    /**
     * 返回拒绝执行的次数
     * @return
     */
    public long getRejectedExecutionCount()
    {
        return m_rejectedCounter.get();
    }
    /**
     * 拒绝执行处理
     *
     * @param worker
     * @param executor
     */
    protected abstract void handleRejectedExecution(SocketWorker worker, ThreadPoolExecutor executor);

}
