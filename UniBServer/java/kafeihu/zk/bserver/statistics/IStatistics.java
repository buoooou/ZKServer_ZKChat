package kafeihu.zk.bserver.statistics;

/**
 * 如果对象需要进行数据统计，实现该接口
 * Created by zhangkuo on 2016/11/25.
 */
public interface IStatistics {
    /**
     * 统计对象类型：Socket服务器
     */
    String StatType_SocketServer = "UBS_SocketServer";
    /**
     * 统计对象类型：心跳接口
     */
    String StatType_KeepAlive = "KeepAlive";
    /**
     * 统计对象类型：对象池
     */
    String StatType_ObjectPool = "UBS_ObjectPool";
    /**
     * 统计对象类型：数据库连接池
     */
    String StatType_DBConnPool = "UBS_DBConnPool";
    /**
     * 统计对象类型：业务处理类
     */
    String StatType_Proc = "UBS_Proc";
    /**
     * 获取统计信息
     * @return
     */
    Object getStatistics();
}
