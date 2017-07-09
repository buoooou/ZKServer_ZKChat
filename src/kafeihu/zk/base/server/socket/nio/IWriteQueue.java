package kafeihu.zk.base.server.socket.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Created by zhangkuo on 2017/7/9.
 */
public interface IWriteQueue {
    /**
     *
     * @return
     */
    boolean isEmpty();
    /**
     *
     * @param channel
     * @return
     * @throws IOException
     */
    int writeChannel(ByteChannel channel) throws IOException;

    /**
     *
     * @param byteBuffer
     * @return
     */
    boolean enqueue(ByteBuffer byteBuffer);

    /**
     *
     * @param byteArray
     * @return
     */
    boolean enqueue(byte[] byteArray);

}
