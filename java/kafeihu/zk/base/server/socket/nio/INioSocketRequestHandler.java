package kafeihu.zk.base.server.socket.nio;

import java.nio.ByteBuffer;

/**
 * Created by zhangkuo on 2017/7/10.
 */
public interface INioSocketRequestHandler {
    /**
     *
     * @param channelFacade
     * @return
     */
    ByteBuffer nextRequestData(IChannelFacade channelFacade);

    /**
     *
     * @param requestBuf
     * @param channelFacade
     * @return
     */
    boolean handleRequestData(ByteBuffer requestBuf, IChannelFacade channelFacade);

    /**
     *
     * @param channelFacade
     */
    void starting(IChannelFacade channelFacade);
    /**
     *
     * @param channelFacade
     */
    void started(IChannelFacade channelFacade);
    /**
     *
     * @param channelFacade
     */
    void stopping(IChannelFacade channelFacade);
    /**
     *
     * @param channelFacade
     */
    void stopped(IChannelFacade channelFacade);
}
