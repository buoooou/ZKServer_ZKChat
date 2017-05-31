package kafeihu.zk.base.client;

import kafeihu.zk.base.util.SocketKit;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public abstract class SocketClient {
    private final static String PackDataFailed = "pack data failed: ";
    private final static String SendRequestFailed = "send request data failed: ";
    private final static String RecvResponseFailed = "receive response data failed: ";

    private Socket m_socket;

    public SocketClient()
    {
        super();
    }

    public SocketClient(Socket mSocket)
    {
        super();
        m_socket = mSocket;
    }

    public SocketClient(String serverIp, int serverPort, int timeoutMills) throws Exception
    {
        super();
        m_socket = new Socket(serverIp, serverPort);
        m_socket.setSoTimeout(timeoutMills);
    }

    public void setSocket(Socket mSocket)
    {
        m_socket = mSocket;
    }

    /**
     * 数据打包
     *
     * @param data
     * @return
     * @throws Exception
     */
    protected abstract byte[] packData(byte[] data) throws Exception;

    /**
     * 接收返回数据
     *
     * @param socket
     * @return
     * @throws Exception
     */
    protected abstract byte[] receive(Socket socket) throws Exception;

    /**
     * 发送请求数据，无返回数据
     *
     * @param data
     * @throws Exception
     */
    public void send(byte[] data) throws IOException,Exception
    {
        String errMsg = "";
        try
        {
            // 打包请求数据
            errMsg = PackDataFailed;
            byte[] packData = packData(data);
            SocketKit sockit = new SocketKit(m_socket);
            // 发送请求数据
            errMsg = SendRequestFailed;
            sockit.send(packData);
        }
        catch (IOException exp)
        {
            IOException ioe = new IOException(errMsg + exp.getMessage());
            ioe.initCause(exp);
            throw ioe;
        }
        catch (Exception exp)
        {
            throw new Exception(errMsg + exp.getMessage(), exp);
        }
        finally
        {
            close();
        }
    }

    /**
     * 发送请求并接受返回数据
     *
     * @param data
     * @param
     * @return
     * @throws Exception
     */
    public byte[] sendRecv(byte[] data) throws IOException,Exception
    {
        String errMsg = "";
        try
        {
            // 打包请求数据
            errMsg = PackDataFailed;
            byte[] packData = packData(data);

            SocketKit sockit = new SocketKit(m_socket);
            // 发送请求数据
            errMsg = SendRequestFailed;
            sockit.send(packData);

            // 接收返回数据
            // m_socket.setSoTimeout(timeoutMills);
            errMsg = RecvResponseFailed;
            return receive(m_socket);
        }
        catch (IOException exp)
        {
            IOException ioe = new IOException(errMsg + exp.getMessage());
            ioe.initCause(exp);
            throw ioe;
        }
        catch (Exception exp)
        {
            throw new Exception(errMsg + exp.getMessage(), exp);
        }
        finally
        {
            close();
        }
    }

    private void close()
    {
        try
        {
            m_socket.close();
        }
        catch (Throwable exp)
        {
        }
        m_socket = null;
    }
}
