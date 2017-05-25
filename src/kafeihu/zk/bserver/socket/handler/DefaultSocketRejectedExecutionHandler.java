package kafeihu.zk.bserver.socket.handler;

import kafeihu.zk.bserver.exception.BServerException;
import kafeihu.zk.bserver.exception.model.ErrorCodeConstants;
import kafeihu.zk.bserver.socket.SocketWorker;
import kafeihu.zk.bserver.socket.SocketWorkerRejectedExecutionHandler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 当SocketServer忙（线程池不能接受任务）时，由 ThreadPoolExecutor 调用的类
 * Created by zhangkuo on 2016/11/25.
 */
public class DefaultSocketRejectedExecutionHandler extends SocketWorkerRejectedExecutionHandler {

    /**
     * 拒绝执行处理：将系统忙信息反馈给客户端
     */
    @Override
    protected void handleRejectedExecution(SocketWorker socketWorker, ThreadPoolExecutor executor)
    {
        BServerException exp = new BServerException(ErrorCodeConstants.ExecutorBusy);
        if (executor.isShutdown())
        {
            exp = new BServerException(ErrorCodeConstants.ExecutorAbnormal);
        }
        ISocketExceptionHandler expHandler = socketWorker.getExceptionHandler();
        //异常处理
        expHandler.handleException(socketWorker.getSocket(), exp);
    }
}
