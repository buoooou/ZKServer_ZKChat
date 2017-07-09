package kafeihu.zk.base.server.socket.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 *
 * 读数据缓冲队列：从Channel中读取数据到缓冲区
 *
 * Created by zhangkuo on 2017/7/9.
 */
public interface IReadQueue {

    /**
     * 从指定Channel读取数据到缓冲区
     *
     * @param channel
     * @return 读取的字节数，可能为零，如果该通道已到达流的末尾，则返回 -1
     * @throws IOException
     */
    int readChannel(ByteChannel channel) throws IOException;

    /**
     * 读缓冲区是否为空
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 指定字节在读缓冲区的首次出现位置
     *
     * @param b
     * @return -1表示缓冲区内不存在指定的字节数据
     */
    int indexOf(byte b);

    /**
     * 获取并删除缓冲区内指定数量的字节数据
     *
     * @param count
     * @return 删除的数据
     */
    ByteBuffer dequeueBytes(int count);

}
