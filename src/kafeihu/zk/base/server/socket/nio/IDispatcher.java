package kafeihu.zk.base.server.socket.nio;

import kafeihu.zk.base.server.socket.ISocketServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

/**
 * Created by zhangkuo on 2017/7/9.
 */
public interface IDispatcher {

    /**
     *
     * @throws IOException
     */
    void dispatch() throws IOException;

    /**
     *
     * @return
     */
    boolean isDispatching();
    /**
     *
     *
     */
    void shutdown();
    /**
     *
     * @param channel
     * @param handler
     * @param readQueue
     * @return
     * @throws IOException
     */
    IChannelFacade registerChannel(ISocketServer socketSrc, SelectableChannel channel, INioSocketRequestHandler handler, IReadQueue readQueue, IWriteQueue writeQueue);
    /**
     *
     * @param adapter
     */
    void unRegisterChannel(IChannelFacade adapter);
    /**
     *
     */
    int getRegisteredChannelNum();
}
