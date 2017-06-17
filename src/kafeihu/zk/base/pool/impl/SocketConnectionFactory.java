package kafeihu.zk.base.pool.impl;

import kafeihu.zk.base.pool.PoolableObjectConfig;
import kafeihu.zk.base.pool.PoolableObjectFactory;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket连接工厂类。负责创建/关闭Socket连接对象
 *
 * Created by zhangkuo on 2017/6/17.
 */
public class SocketConnectionFactory implements PoolableObjectFactory {
    /**
     * Socket连接配置类
     */
    private SocketConnectionConfig m_config;

    protected SocketConnectionFactory()
    {
        super();
    }

    /**
     * 构造函数
     *
     * @param mConfig
     */
    public SocketConnectionFactory(SocketConnectionConfig mConfig)
    {
        super();
        m_config = mConfig;
    }

    /**
     * 构造函数。
     *
     * @param xmlConfig
     *            Xml格式的配置数据。格式为：<br>
     *            <socketserverIP>addr</socketserverIP>
     *            <socketserverPort>port</socketserverPort>
     *            <socketTimeoutMills>mills</socketTimeoutMills>
     */
    public SocketConnectionFactory(String xmlConfig) throws Exception
    {
        super();
        m_config = SocketConnectionConfig.parseSocketConnectionConfig(xmlConfig);
    }

    /**
     * 关闭并销毁Socket连接对象
     */
    public void destroyObject(Object obj) throws Exception
    {
        if (null == obj)
        {
            return;
        }
        Socket sock;
        try
        {
            sock = (Socket) obj;
            sock.close();
        }
        catch (Exception exp)
        {
            throw new Exception(getClass().getName() + ".destroyObject(). " + exp, exp);
        }
        finally
        {
            sock = null;
        }
    }

    /**
     * 构造Socket连接对象
     */
    public Object makeObject() throws Exception
    {
        try
        {
            Socket sock = new Socket();
            sock.setSoTimeout(m_config.getSoTimeoutMills());
            sock.setReuseAddress(true);
            sock.connect(new InetSocketAddress(m_config.getAddress(), m_config.getPort()), m_config
                    .getSoConnTimeoutMills());
            return sock;
        }
        catch (Exception exp)
        {
            throw new Exception(getClass().getName() + ".makeObject(). " + exp, exp);
        }
    }

    /**
     * 保持Socket连接处于可用状态
     */
    public void activateObject(Object obj) throws Exception
    {
        if (null == obj)
        {
            return;
        }
        Socket sock = (Socket) obj;
        sock.sendUrgentData(0);
    }

    /**
     * 挂起Socket连接
     */
    public void passivateObject(Object obj) throws Exception
    {
    }

    /**
     * 判断Socket连接是否有效（Socket服务器端是否已断开连接）<br>
     * 发送UrgentData到服务器端，如果服务器端Socket的SO_OOBINLINE未打开（默认为关闭），则服务器会自动丢弃该数据
     */
    public boolean validateObject(Object obj)
    {
        boolean bValid = true;
        try
        {
            activateObject(obj);
        }
        catch (Exception e)
        {
            bValid = false;
        }
        return bValid;
    }

    /**
     * 根据指定的Socket连接配置构造Socket连接对象
     */
    public Object makeObject(PoolableObjectConfig poolObjConfig) throws Exception
    {
        SocketConnectionConfig connConfig = (SocketConnectionConfig)poolObjConfig;
        try
        {
            Socket sock = new Socket();
            sock.setSoTimeout(connConfig.getSoTimeoutMills());
            sock.setReuseAddress(true);
            sock.connect(new InetSocketAddress(connConfig.getAddress(), connConfig.getPort()),
                    connConfig.getSoConnTimeoutMills());
            return sock;
        }
        catch (Exception exp)
        {
            throw new Exception(SocketConnectionFactory.class.getName() + ".makeObject(). " + exp,
                    exp);
        }
    }

    @Override
    public String toString()
    {
        if (null == m_config)
        {
            return getClass().getName();
        }
        return m_config.toString();
    }

    @Override
    public String targetInfo()
    {
        StringBuilder sb = new StringBuilder();
        //sb.append("Socket ");
        sb.append(m_config.getAddress().getHostAddress());
        sb.append(":");
        sb.append(m_config.getPort());

        return sb.toString();
    }
}
