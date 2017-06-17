package kafeihu.zk.database;

/**
 * //数据库连接池相关常量
 *
 * Created by zhangkuo on 2016/11/22.
 */
public class DBConstants {

    /**
     * 连接池初始连接数缺省值
     */
    public static int POOL_InitialSize = 1;
    /**
     * 连接池最大连接数缺省值
     */
    public static int POOL_MaxSize = 8;
    /**
     * 连接池最小连接数缺省值
     */
    public static int POOL_MinSize = 1;
    /**
     * 连接空闲时间缺省值。单位：秒
     */
    public static int POOL_MaxIdleSeconds = 60;
    /**
     * 空闲连接扫描线程执行间隔缺省值。单位：秒
     */
    public static int POOL_IdleConnScanSpanSeconds = 60;
    /**
     * 获取连接最大等待时间缺省值。单位：毫秒
     */
    public static long POOL_MaxWaitTimeoutMills = 500;
    /**
     * 连接被占用时间过长告警阀值。单位毫秒
     */
    public static long POOL_ConnAccessSpanWarningMills = 1000;
    /**
     * 允许的数据库连接后台构造线程数缺省值。默认为1个
     */
    public static int POOL_MaxCreateThreadCount = 1;
    //其他常量
    public static String Msg_UnsupportedMethod = "unsupported method";
}
