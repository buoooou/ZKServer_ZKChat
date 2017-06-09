package kafeihu.zk.bserver.monitor;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.util.IPPattern;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.SocketKit;
import kafeihu.zk.bserver.manager.LoggerManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * 监控服务监听类：接受并执行监控请求，返回处理结果<br>
 * 监控请求格式：4byte数据长度+64byte请求类型(prid)+请求内容
 *
 * Created by zhangkuo on 2017/6/4.
 */
public class MonitorListener implements Runnable {

    private ServerSocket m_socketserver=null;

    private IPPattern m_clientIPPatten=null;

    private boolean m_isRunning=true;

    private boolean m_allowing=true;

    private final Logger m_logger= LoggerManager.getSysLogger();

    public MonitorListener(ServerSocket m_socketserver) {
        super();
        this.m_socketserver = m_socketserver;
    }

    public void setClientIPPatten(IPPattern m_clientIPPatten) {
        this.m_clientIPPatten = m_clientIPPatten;
    }

    public void setAllow(boolean m_allowing) {
        this.m_allowing = m_allowing;
    }

    public void start(){

        new Thread(this).start();

    }


    public void stop(){

        m_isRunning=false;


        try {
            m_socketserver.close();
        } catch (IOException e) {
            m_logger.error(this.getClass().getName(),"stop Monitorserver filed:"+e.getMessage());
        }finally {
            m_socketserver=null;
        }

    }

    @Override
    public void run() {

        while (m_isRunning){

            Socket socket=acceptSocket();
            if(socket==null){
                continue;
            }



            SocketKit socketKit=new SocketKit(socket);

            try {
                acceptClinetIP(socket);
                // 接收数据长度
                byte []packlen =new byte[MonitorProc.PACK_LEN];
                socketKit.receive(packlen,MonitorProc.PACK_LEN);

                int iLen= MiscUtil.ntohl(packlen);

                // 接收数据
                byte[] data = new byte[iLen];
                socketKit.receive(data,iLen);

                String requestData = new String(data);

                String prid=requestData.substring(0,MonitorProc.PRID_LEN);

                if(MiscUtil.isEmpty(prid)){
                    throw new Exception("prid can not be empty");
                }

                MonitorProc monProc = MonitorManager.getProc(prid);

                String request = requestData.substring(MonitorProc.PRID_LEN);

                monProc.doProc(socket, request);


            } catch (Exception exp) {
                MonitorProc.sendResponse(socket, MonitorProc.FailPrefix, "MonitorProc:"
                        + exp.getMessage());
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {

                }

            }
        }

    }

    //过滤客户端
    private void acceptClinetIP(Socket socket) throws Exception{

        if (null == m_clientIPPatten)
        {
            return;
        }
        InetAddress clientAddr = socket.getInetAddress();
        if (null != clientAddr)
        {
            boolean bMatch = m_clientIPPatten.match(clientAddr.getHostAddress());
            if (!(m_allowing && bMatch))
            {// 允许匹配的客户端地址
                m_logger.warn(getClass().getName(), "illegal Monitor Client:"
                        + clientAddr.getHostAddress());
                throw new Exception("illegal Monitor Client");
            }
        }
    }

    private Socket acceptSocket() {

        try {
            return m_socketserver.accept();
        } catch (IOException e) {
            m_logger.error(this.getClass().getName(),"acceptSocket filed:"+e.getMessage());
        }
        return null;

    }
}
