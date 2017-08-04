package kafeihu.zk.database;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.database.pool.DBConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * 数据库处理工作者类，用于异步执行数据库操作
 *
 * Created by zhangkuo on 2017/6/17.
 */
public abstract class DBProcWorker implements Callable<DBProcResult> {

    private DBConnectionPool dbConnPool;

    public DBProcWorker(DBConnectionPool dbConnPool)
    {
        this.dbConnPool = dbConnPool;
    }

    /**
     * 通过指定的数据库连接完成特定的数据库处理，子类需重新该方法实现特定处理
     *
     * @param conn
     *            数据库连接实例，使用完不需要执行连接关闭方法，框架会自动执行
     * @return 数据库处理结果
     * @throws SQLException
     *             数据库操作异常
     * @throws Exception
     *             其他异常
     */
    abstract protected Object doDBProc(Connection conn) throws SQLException, Exception;

    /**
     * 异常处理接口，数据库处理过程中发生异常时被调用
     *
     * @param exp
     */
    protected void onException(Exception exp)
    {
        StringBuilder sbErr = new StringBuilder();
        sbErr.append("Exception occurs.");
        sbErr.append(" ErrorMsg:").append(exp.getMessage());
        getLogger().error(getClass().getName(), sbErr.toString());
    }

    /**
     * 异常处理接口，数据库处理过程中发生SQL异常时被调用
     *
     * @param exp
     */
    protected void onSQLException(SQLException exp)
    {
        StringBuilder sbErr = new StringBuilder();
        sbErr.append("SQLException occurs.");
        sbErr.append(" SQLState:").append(exp.getSQLState());
        sbErr.append(" ErrorCode:").append(exp.getErrorCode());
        sbErr.append(" ErrorMsg:").append(exp.getMessage());
        getLogger().error(getClass().getName(), sbErr.toString());
    }

    /**
     * 返回数据库连接池
     *
     * @return
     */
    protected DBConnectionPool getDBConnectionPool()
    {
        return dbConnPool;
    }

    /**
     * 返回日志处理器
     *
     * @return
     */
    protected Logger getLogger()
    {
        return dbConnPool.getLogger();
    }

    @Override
    public DBProcResult call() throws Exception
    {
        return doDBProc();
    }

    /**
     * 执行数据库处理
     *
     * @return 数据库处理结果
     */
    public DBProcResult doDBProc()
    {
        Connection conn = null;
        try
        {
            // 获取数据库连接
            conn = dbConnPool.getDBConnection();
            conn.setAutoCommit(true);
            // 执行数据库处理
            Object result = doDBProc(conn);
            // 构造处理结果实例并返回
            return new DBProcResult(result);
        }
        catch (SQLException exp)
        {
            // 异常处理
            onSQLException(exp);
            // 构造处理结果实例并返回
            DBProcResult result = new DBProcResult();
            result.setException(exp);
            return result;
        }
        catch (Exception exp)
        {
            // 异常处理
            onException(exp);
            // 构造处理结果实例并返回
            DBProcResult result = new DBProcResult();
            result.setException(exp);
            return result;
        }
        finally
        {
            if (null != conn)
            {
                try
                {
                    conn.close();
                }
                catch (Throwable t)
                {
                }
            }
        }

    }

}
