package kafeihu.zk.base.socket.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * Socket工具类。包装了Socket读、写处理
 * Created by zhangkuo on 2016/11/25.
 */
public class SocketKit {
    private Socket m_socket;

    /**
     * 通过IP,Port构造SocketKit<br>
     * Socket的附加属性设置:调用getSocket获取Socket，然后进行设置。
     *
     * @param ip
     * @param port
     * @throws Exception
     */
    public SocketKit(String ip, int port) throws Exception
    {
        m_socket = new Socket(ip, port);
    }

    /**
     * 以特定的Socket构造SocketKit<br>
     * Socket的附加属性设置在调用该构造函数前进行。
     *
     * @param socket
     */
    public SocketKit(Socket socket)
    {
        m_socket = socket;
    }

    /**
     * 发送字节数组数据。
     *
     * @param baBuffer
     *            要发送的数据
     * @throws Exception
     */
    public void send(byte[] baBuffer) throws IOException
    {
//		try
//		{
        OutputStream out = m_socket.getOutputStream();
        out.write(baBuffer);
//		}
//		catch (Exception e)
//		{
//			throw new Exception(this.getClass().getName() + ".send(). " + e, e);
//		}
    }

    /**
     * 接受数据到字节数组。
     *
     * @param baBuffer
     *            接受数据存放的字节数组
     * @param nMaxReceiveByte
     *            最大接收字节数
     * @return 实际接收的字节数
     * @throws Exception
     */
    public int receive(byte[] baBuffer, int nMaxReceiveByte) throws IOException
    {
        return receive(baBuffer, 0, nMaxReceiveByte);
    }

    /**
     * 接受数据到字节数组。从字节数组的指定位置开始存放接受数据。
     *
     * @param baBuffer
     *            接受数据存放的字节数组
     * @param off
     *            字节数组存放数据的开始位置
     * @param nMaxReceiveByte
     *            最大接收字节数
     * @return 实际接收的字节数
     * @throws Exception
     */
    public int receive(byte[] baBuffer, int off, int nMaxReceiveByte) throws IOException
    {
//		try
//		{
        InputStream in = m_socket.getInputStream();

        int nTotalReaded = 0;
        while (nTotalReaded < nMaxReceiveByte)
        {
            int nThisTimeReaded = in.read(baBuffer, nTotalReaded + off, nMaxReceiveByte - nTotalReaded);
            if (nThisTimeReaded < 0)
            {// 达到EOF，返回-1
                if(0 == nTotalReaded)
                {
                    return -1;
                }
                break;
            }
            nTotalReaded += nThisTimeReaded;
        }

        return nTotalReaded;
//		}
//		catch (Exception e)
//		{
//			throw new Exception(this.getClass().getName() + ".receive(). " + e, e);
//		}
    }

    /**
     * 关闭关联的Socket连接
     */
    public void close()
    {
        try
        {
            if (m_socket != null)
            {
                m_socket.close();
                m_socket = null;
            }
        }
        catch (Exception e)
        {
        }
    }

    /**
     * 获取关联的Socket连接
     *
     * @return
     */
    public Socket getSocket()
    {
        return m_socket;
    }
}
