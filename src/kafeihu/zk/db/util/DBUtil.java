package kafeihu.zk.db.util;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by zhangkuo on 2016/11/22.
 */
public class DBUtil {
    /**
     * 关闭数据库操作Statement。如PreparedStatement、CallableStatement
     * @param pstStatement
     */
    public static void closeSqlStatement(Statement pstStatement)
    {
        if (pstStatement != null)
        {
            try
            {
                pstStatement.close();
                pstStatement = null;
            }
            catch (Exception e)
            {
            }
        }
    }
    /**
     * 关闭数据库连接
     * @param conn
     */
    public static void closeConnection(Connection conn)
    {
        if (conn != null)
        {
            try
            {
                conn.close();
                conn = null;
            }
            catch (Exception e)
            {
            }
        }
    }
}
