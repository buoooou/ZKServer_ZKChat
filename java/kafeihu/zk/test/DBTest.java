package kafeihu.zk.test;

import kafeihu.zk.bserver.manager.DBConnectionPoolManager;
import kafeihu.zk.database.pool.DBConnectionPool;

import java.sql.Connection;

/**
 * Created by zhangkuo on 2016/11/23.
 */
public class DBTest {

    public static void main(String[] args) {

        try {
            DBConnectionPool dbpool=DBConnectionPoolManager.getModuleDBConnectionPool("zkchat","NETPAY");
            Connection conn=dbpool.getDBConnection();
            System.out.println(conn);
            String sql="";

            System.out.println("2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
