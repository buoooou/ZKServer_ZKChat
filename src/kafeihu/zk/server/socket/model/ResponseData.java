package kafeihu.zk.server.socket.model;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class ResponseData {
    /**
     * 结果数据
     */
    private byte[] data;

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }
}
