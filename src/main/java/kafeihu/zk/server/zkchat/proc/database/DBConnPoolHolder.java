package kafeihu.zk.server.zkchat.proc.database;

import kafeihu.zk.base.logging.slf4j.Slf4JUtil;
import kafeihu.zk.server.manager.DBConnectionPoolManager;
import kafeihu.zk.server.zkchat.core.ZKChatErrorCode;
import kafeihu.zk.server.zkchat.core.ZKChatException;
import kafeihu.zk.database.pool.DBConnectionPool;

/**
 * 数据库连接池实例持有者：应用通过该类获取特定的数据库连接池
 *
 *
 * Created by zhangkuo on 2017/6/18.
 */
public class DBConnPoolHolder {
    private DBConnPoolHolder()
    {
    }

    /**
     * 数据库连接池实例。目标数据库为网上支付数据库NETPAY
     */
    public static DBConnectionPool NETPAY;
    /**
     * 数据库连接池实例。目标数据库为专业版数据库PB
     */
    // public static DBConnectionPool PB;

    static
    {
        try
        {
            Slf4JUtil.getConsoleLogger().info("Initializing DBConnPoolHolder...... ");
            initialize();
            Slf4JUtil.getConsoleLogger().info("OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(DBConnPoolHolder.class.getName() + ".initialize(). " + exp);
        }
    }

    /**
     * 数据库实例初始化
     *
     * @throws Exception
     */
    private static void initialize() throws Exception
    {
        NETPAY = DBConnectionPoolManager.getSysDBConnectionPool("zkchat");
    }

    /**
     * 通过数据库ID获取数据库连接池
     * @param dbID
     * @return
     * @throws ZKChatException
     */
    public static DBConnectionPool getDBConnPool(String dbID) throws ZKChatException
    {
        try
        {
            return DBConnectionPoolManager.getSysDBConnectionPool(dbID);
        }
        catch (Exception exp)
        {
            throw new ZKChatException(ZKChatErrorCode.DBProc_Exception, exp.getMessage());
        }
    }
}
