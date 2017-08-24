package kafeihu.zk.base.pool.impl;

import kafeihu.zk.base.pool.PoolableObjectConfig;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * Socket连接配置类：配置Socket连接的基本信息。
 *
 * Created by zhangkuo on 2017/6/17.
 */
public class SocketConnectionConfig implements PoolableObjectConfig {

    /**
     * Socket服务器地址
     */
    private InetAddress address;
    /**
     * Socket服务器监听端口
     */
    private int port;
    /**
     * Socket相关操作超时：读、写等。默认为15秒
     */
    private int soTimeoutMills = 15000;

    /**
     * Socket连接超时。默认5秒
     */
    private int soConnTimeoutMills = 5000;
    /**
     * 构造函数
     *
     * @param address
     *            Socket服务器地址
     * @param port
     *            Socket服务器监听端口
     */
    public SocketConnectionConfig(InetAddress address, int port)
    {
        super();
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress()
    {
        return address;
    }

    public void setAddress(InetAddress address)
    {
        this.address = address;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * 获取Socket相关操作超时时间，单位：毫秒
     *
     * @return
     */
    public int getSoTimeoutMills()
    {
        return soTimeoutMills;
    }

    /**
     * 设置Socket相关操作超时时间，单位：毫秒
     *
     * @param soTimeoutMills
     */
    public void setSoTimeoutMills(int soTimeoutMills)
    {
        this.soTimeoutMills = soTimeoutMills;
    }

    public int getSoConnTimeoutMills()
    {
        return soConnTimeoutMills;
    }

    public void setSoConnTimeoutMills(int soConnTimeoutMills)
    {
        this.soConnTimeoutMills = soConnTimeoutMills;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SocketConnectionConfig other = (SocketConnectionConfig) obj;
        if (address == null)
        {
            if (other.address != null)
                return false;
        }
        else if (!address.equals(other.address))
            return false;
        if (port != other.port)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "SocketConnection. address=" + address + ", port=" + port;
    }
    /**
     * 将Xml格式的配置数据解析为一个SocketConnectionConfig实例。配置数据格式为：<br>
     * <socketserverIP>addr</socketserverIP>
     * <socketserverPort>port</socketserverPort>
     * <socketTimeoutMills>mills</socketTimeoutMills>
     *
     * @param xmlConfig
     * @return
     * @throws Exception
     */
    public static SocketConnectionConfig parseSocketConnectionConfig(String xmlConfig)
            throws Exception
    {
        String socketserverIP = XmlUtil.getXmlElement("socketserverIP", xmlConfig);
        if (MiscUtil.isEmpty(socketserverIP))
        {
            throw new Exception("socketserverIP can not be empty");
        }
        InetAddress socketServerAddr;
        try
        {
            socketServerAddr = InetAddress.getByName(socketserverIP);
        }
        catch (UnknownHostException exp)
        {
            throw new Exception("illegal socketserverIP value:" + socketserverIP);
        }
        int socketTimeoutMills;
        String tmpValue = "";
        try
        {
            tmpValue = XmlUtil.getXmlElement("socketTimeoutMills", xmlConfig, "15000");
            socketTimeoutMills = Integer.parseInt(tmpValue);
            if (socketTimeoutMills <= 0)
            {
                socketTimeoutMills = 15000;
            }
        }
        catch (Exception exp)
        {
            throw new Exception("illegal socketTimeoutMills value:" + tmpValue);
        }

        int socketConnTimeoutMills;
        try
        {
            tmpValue = XmlUtil.getXmlElement("socketConnTimeoutMills", xmlConfig, "5000");
            socketConnTimeoutMills = Integer.parseInt(tmpValue);
            if (socketConnTimeoutMills <= 0)
            {
                socketConnTimeoutMills = 5000;
            }
        }
        catch (Exception exp)
        {
            throw new Exception("illegal socketConnTimeoutMills value:" + tmpValue);
        }

        int socketServerPort;
        try
        {
            tmpValue = XmlUtil.getXmlElement("socketserverPort", xmlConfig);
            socketServerPort = Integer.parseInt(tmpValue);
        }
        catch (Exception e)
        {
            throw new Exception("illegal socketserverPort value:" + tmpValue);
        }
        SocketConnectionConfig config = new SocketConnectionConfig(socketServerAddr,
                socketServerPort);
        config.setSoTimeoutMills(socketTimeoutMills);
        config.setSoConnTimeoutMills(socketConnTimeoutMills);
        return config;
    }

}
