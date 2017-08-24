package kafeihu.zk.bserver.service;

/**
 * Created by zhangkuo on 2016/11/24.
 */
public abstract class ServiceManager {
    /**
     * 启动服务
     *
     * @throws Exception
     */
    public static void startService() throws Exception
    {
        throw new UnsupportedOperationException("not implement method startService() yet");
    }

    /**
     * 停止服务
     *
     * @throws Exception
     */
    public static void stopService() throws Exception
    {
        throw new UnsupportedOperationException("not implement method stopService() yet");
    }

    /**
     * 启动服务
     *
     * @param param
     *            要启动的服务的附加参数，如特定的端口号等
     * @throws Exception
     */
    public static void startService(Object param) throws Exception
    {
        startService();
    }

    /**
     * 停止服务
     *
     * @param param
     *            要停止的服务的附加参数，如特定的端口号等
     * @throws Exception
     */
    public static void stopService(Object param) throws Exception
    {
        stopService();
    }
}
