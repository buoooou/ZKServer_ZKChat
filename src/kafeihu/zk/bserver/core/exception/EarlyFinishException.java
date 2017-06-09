package kafeihu.zk.bserver.core.exception;

/**
 * 提前结束处理异常：如果某一个ISocketRequestHandler需要提前终止整个请求处理过程，抛出该异常。<br>
 * SocketWorker捕获该异常后会直接丢弃，并终止当前处理线程<br>
 * 注意：抛出该线程时，应该已完成与客户端的交互
 *
 * Created by zhangkuo on 2016/11/25.
 */
public class EarlyFinishException extends Exception{
}
