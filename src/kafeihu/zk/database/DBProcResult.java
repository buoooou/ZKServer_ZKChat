package kafeihu.zk.database;

import java.sql.SQLException;

/**
 * 数据库处理结果类
 *
 * Created by zhangkuo on 2017/6/17.
 */
public class DBProcResult {
    private Exception exp = null;

    private Object result = null;

    private boolean isSQLException = false;

    public DBProcResult()
    {
    }

    public DBProcResult(Object result)
    {
        this.result = result;
    }

    /**
     * 获取数据库处理结果
     * @return
     */
    public Object getResult()
    {
        return result;
    }

    /**
     * 数据库处理是否成功
     * @return
     */
    public boolean isSucc()
    {
        return null == exp;
    }

    /**
     * 设置处理异常
     * @param exp
     */
    public void setException(Exception exp)
    {
        if (exp instanceof SQLException)
        {
            isSQLException = true;
        }
        else
        {
            isSQLException = false;
        }
        this.exp = exp;
    }

    /**
     * 是否为发生了SQL异常
     * @return
     */
    public boolean isSQLException()
    {
        return isSQLException;
    }

    /**
     * 获取数据库处理异常
     * @return 异常实例，可能为 DBPoolException, SQLException, Exception
     */
    public Exception getException()
    {
        return exp;
    }
    /**
     * 数据库处理失败错误信息
     * @return
     */
    public String getErrorMsg()
    {
        String errMsg = "";
        if (null != exp)
        {
            errMsg = exp.getMessage();
        }
        if (null == errMsg)
        {
            errMsg = "";
        }
        return errMsg;
    }
}
