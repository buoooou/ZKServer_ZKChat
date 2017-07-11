package kafeihu.zk.base.server.socket.nio;

import java.nio.ByteBuffer;

/**
 *
 * 读数据缓冲队列：按特定数据包格式从Channel中读取数据
 *
 * Created by zhangkuo on 2017/7/11.
 */
public interface IPacketReadQueue extends IReadQueue{
    /**
     * 从缓冲区中获取并删除一个完整的数据包
     *
     * @return 如果完整的数据包还未接收完整，返回null
     */
    ByteBuffer dequeuePacket();

    /**
     * 是否已完整接收到数据包
     *
     * @return
     */
    boolean hasPacket();
    /**
     * 返回数据包长度，如果尚未接收到长度信息，返回-1
     * @return
     */
    int packetLength();
}
