package kafeihu.zk.base.server;

/**
 * Created by zhangkuo on 2016/11/24.
 */
public interface IServer {
    /**
     * Server运行状态
     *
     * @return
     */
    boolean isRunning();

    /**
     * 启动Server时调用
     */
    void onServerStarting();

    /**
     * 启动Server
     *
     * @return Server运行线程
     * @throws Exception
     */
    Thread start() throws Exception;

    /**
     * 启动Server后调用
     */
    void onServerStarted();

    /**
     * 停止Server前调用
     */
    void onServerStopping();

    /**
     * 停止Server
     *
     * @throws Exception
     */
    void stop() throws Exception;

    /**
     * 停止Server后调用
     */
    void onServerStopped();

    /**
     * 获取服务器别名
     *
     * @return
     */
    String getAlias();

    /**
     * 获取当前在线的客户数
     *
     * @return
     */
    int getOnlineClientNum();

}
