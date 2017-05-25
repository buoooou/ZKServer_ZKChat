package kafeihu.zk.server.socket.model;

/**
 * BServer业务请求数据
 * Created by zhangkuo on 2016/11/25.
 */
public class RequestData {
    /**
     * BServer服务模块名
     */
    private String moduleName;
    /**
     * 具体的Proc处理类ID
     */
    private String procId;
    /**
     * 请求数据
     */
    private byte[] data;

    public String getModuleName()
    {
        return moduleName;
    }

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName.trim();
    }

    public String getProcId()
    {
        return procId;
    }

    public void setProcId(String procId)
    {
        this.procId = procId.trim();
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

}
