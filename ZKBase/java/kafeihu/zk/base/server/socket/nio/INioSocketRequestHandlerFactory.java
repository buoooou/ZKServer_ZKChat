package kafeihu.zk.base.server.socket.nio;

/**
 * 请求处理器工厂类接口
 *
 * Created by zhangkuo on 2017/7/9.
 */
public interface INioSocketRequestHandlerFactory {
    /**
     * 构造新的请求处理器实例
     * @return
     */
    INioSocketRequestHandler newRequestHandler();

    /**
     * 构造读缓冲队列
     *
     * @return
     */
    IReadQueue createReadQueue();


    /**
     * 构造写缓冲队列。子类可重写该方法构造特定的写队列
     *
     * @return
     */
    IWriteQueue createWriteQueue();
}
