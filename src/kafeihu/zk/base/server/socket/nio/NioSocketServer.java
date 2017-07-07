package kafeihu.zk.base.server.socket.nio;

import kafeihu.zk.base.server.socket.ISocketServer;

import java.net.Socket;

/**
 * Created by zhangkuo on 2017/7/7.
 */
public class NioSocketServer implements ISocketServer{
    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void onServerStarting() {

    }

    @Override
    public Thread start() throws Exception {
        return null;
    }

    @Override
    public void onServerStarted() {

    }

    @Override
    public void onServerStopping() {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void onServerStopped() {

    }

    @Override
    public String getAlias() {
        return null;
    }

    @Override
    public int getOnlineClientNum() {
        return 0;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public void onSocketAccepted(Socket socket) {

    }

    @Override
    public void onSocketClosing(Socket socket) {

    }

    @Override
    public void onSocketClosed(Socket socket) {

    }

    @Override
    public void setClientSocketTimeoutMills(int mills) {

    }
}
