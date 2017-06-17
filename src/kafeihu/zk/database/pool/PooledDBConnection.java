package kafeihu.zk.database.pool;

import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.database.DBConstants;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhangkuo on 2016/11/22.
 */
public class PooledDBConnection implements Connection
{

    // 真正的数据库连接
    private Connection dbConn;
    // 关联的数据库连接池
    private DBConnectionPool dbConnPool;
    // 日志处理器
    private Logger logger = Logger.getConsoleLogger();
    private String logTips = getClass().getName();

    // 连接构造时间
    private long birthTimeMillis = 0L;
    // 该连接上次被使用开始时间
    private long lastAccessStartMillis = 0L;
    // 该连接上次被使用结束时间
    private long lastAccessEndMillis = 0L;
    // 该连接上次被使用时长
    private long lastAccessSpanMills = 0;
    // 该连接是否在连接池中
    private boolean isInPool = true;
    // 累计使用次数
    private AtomicLong accessCount = new AtomicLong(0);
    // 数据库连接是否有效
    private volatile boolean isValid = true;
    // 执行的Sql语句记录
    private Set<String> sqlList = new LinkedHashSet<String>();

    public PooledDBConnection(Connection dbConn, DBConnectionPool dbConnPool)
    {
        super();
        this.dbConn = dbConn;
        this.dbConnPool = dbConnPool;
        logger = dbConnPool.getLogger();
        birthTimeMillis = System.currentTimeMillis();
    }

    private void transactionRecord(String sql)
    {
        sqlList.add(sql);
    }

    protected void checkValid()
    {
        isValid = dbConnPool.isValidDBConnection(this);
    }

    private void handleException(SQLException exp)
    {
        handleException(null, exp);
    }

    private void handleException(String sql, SQLException exp)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("dbConnPool:").append(dbConnPool.getID());
        sb.append(" sqlException:").append(exp);

        if (null != sql)
        {
            sb.append(" sql:").append(sql);
        }
        // 写文件日志
        logger.error(logTips, sb.toString());
        // 统计ＳＱＬ异常数据
//        SQLExceptionMonitor.logException(dbConnPool.getID(), exp);
        // 检查数据库连接有效性
        checkValid();
    }

    /**
     * 该连接被使用（从连接池中返回客户）前调用。框架调用
     */
    protected void beforeAccess()
    {
        // 连接被获取，不在池中
        isInPool = false;
        // 设置该连接最近被使用（访问）开始时间
        lastAccessStartMillis = System.currentTimeMillis();
        // 清空执行历史
        sqlList.clear();
    }

    /**
     * 该连接返回连接池后调用。框架调用
     */
    private void afterAccess()
    {
        // 使用次数加一
        accessCount.incrementAndGet();
        // 更新最近使用时间
        lastAccessEndMillis = System.currentTimeMillis();
        lastAccessSpanMills = lastAccessEndMillis - lastAccessStartMillis;

        if (lastAccessSpanMills > dbConnPool.getConnAccessSpanWarningMills())
        {
            // 连接占用时间超过预警阀值，写告警日志
            StringBuilder sb = new StringBuilder();
            sb.append("timeConsumingMills:").append(lastAccessSpanMills);
            sb.append(" sql:");
            for (String sql : sqlList)
            {
                sb.append(sql).append(" ");
            }
            logger.warn(logTips, sb.toString());
        }
    }

    /**
     * 获取关联的连接池ID
     *
     * @return
     */
    public String getConnectionPoolID()
    {
        return dbConnPool.getID();
    }

    public Logger getLogger()
    {
        return logger;
    }

    /**
     * 获取该连接创建时间
     *
     * @return
     */
    public long getBirthTimeMillis()
    {
        return birthTimeMillis;
    }

    /**
     * 获取该连接最近被使用（访问）结束时间
     *
     * @return
     */
    public long getLastAccessEndMillis()
    {
        return lastAccessEndMillis;
    }

    /**
     * 该连接最近被使用（访问）时长
     *
     * @return
     */
    public long getLastAccessSpanMillis()
    {
        return lastAccessSpanMills;
    }

    public boolean isValid()
    {
        return isValid;
    }

    /**
     * 该连接被使用次数
     *
     * @return
     */
    public long getAccessCount()
    {
        return accessCount.get();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(logTips).append(" [");
        sb.append("isValid=").append(isValid);
        sb.append(", accessCount=").append(accessCount);
         sb.append(", lastAccessTime=").append(MiscUtil.getTimestamp(new
         Date(lastAccessEndMillis)));
        sb.append(", idleSeconds=")
                .append((int) ((System.currentTimeMillis() - lastAccessEndMillis) / 1000));
        sb.append(", surviveSeconds=").append(
                (int) ((lastAccessEndMillis - birthTimeMillis) / 1000));
        sb.append(", dbConn=").append(dbConn);
        sb.append("]");
        return sb.toString();
    }

    /**
     * 获取底层真正的数据库连接实例。该方法由框架调用，应用开发时禁止直接调用该方法
     *
     * @return
     */
    protected Connection getRealDBConnection()
    {
        return dbConn;
    }

    // Override method of java.sql.Connection

    @Override
    public void close() throws SQLException
    {
        if (isInPool)
        {
            // 避免重复返回连接池
            logger.warn(logTips,
                    "util repeat close:" + dbConnPool.getID());
            return;
        }
        // 设置返回池中标志
        isInPool = true;
        // 返回数据库连接池
        dbConnPool.returnDBConnection(this);
        // 后处理
        afterAccess();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        try
        {
            return dbConn.isWrapperFor(iface);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }

    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        try
        {
            return dbConn.unwrap(iface);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        try
        {
            dbConn.clearWarnings();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void commit() throws SQLException
    {
        try
        {
            dbConn.commit();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements)
            throws SQLException
    {
        try
        {
            return dbConn.createArrayOf(typeName, elements);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Blob createBlob() throws SQLException
    {
        try
        {
            return dbConn.createBlob();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Clob createClob() throws SQLException
    {
        try
        {
            return dbConn.createClob();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public NClob createNClob() throws SQLException
    {
        try
        {
            return dbConn.createNClob();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        try
        {
            return dbConn.createSQLXML();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Statement createStatement() throws SQLException
    {
        throw new SQLException(DBConstants.Msg_UnsupportedMethod);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException
    {
        throw new SQLException(DBConstants.Msg_UnsupportedMethod);
    }

    @Override
    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        throw new SQLException(DBConstants.Msg_UnsupportedMethod);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes)
            throws SQLException
    {
        try
        {
            return dbConn.createStruct(typeName, attributes);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setSchema(String schema) throws SQLException {

    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
        try
        {
            return dbConn.getAutoCommit();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public String getCatalog() throws SQLException
    {
        try
        {
            return dbConn.getCatalog();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Properties getClientInfo() throws SQLException
    {
        try
        {
            return dbConn.getClientInfo();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public String getClientInfo(String name) throws SQLException
    {
        try
        {
            return dbConn.getClientInfo(name);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public int getHoldability() throws SQLException
    {
        try
        {
            return dbConn.getHoldability();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        try
        {
            return dbConn.getMetaData();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        try
        {
            return dbConn.getTransactionIsolation();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException
    {
        try
        {
            return dbConn.getTypeMap();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        try
        {
            return dbConn.getWarnings();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        try
        {
            return dbConn.isClosed();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        try
        {
            return dbConn.isReadOnly();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public boolean isValid(int timeout) throws SQLException
    {
        try
        {
            return dbConn.isValid(timeout);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        try
        {
            return dbConn.nativeSQL(sql);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        transactionRecord(sql);
        try
        {
            return dbConn.prepareCall(sql);
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException
    {
        transactionRecord(sql);
        try
        {
            return dbConn.prepareCall(sql, resultSetType, resultSetConcurrency);
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        transactionRecord(sql);
        try
        {
            return dbConn.prepareCall(sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        transactionRecord(sql);
        try
        {
            PreparedStatement preparedStmt = dbConn.prepareStatement(sql);
            if (dbConnPool.isMonitorSwitchOn())
            {
                return new PooledPreparedStatement(sql, preparedStmt, this);
            }
            else
            {
                return preparedStmt;
            }
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException
    {
        transactionRecord(sql);
        try
        {
            PreparedStatement preparedStmt = dbConn.prepareStatement(sql,
                    autoGeneratedKeys);
            if (dbConnPool.isMonitorSwitchOn())
            {
                return new PooledPreparedStatement(sql, preparedStmt, this);
            }
            else
            {
                return preparedStmt;
            }
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws SQLException
    {
        transactionRecord(sql);
        try
        {
            PreparedStatement preparedStmt = dbConn.prepareStatement(sql,
                    columnIndexes);
            if (dbConnPool.isMonitorSwitchOn())
            {
                return new PooledPreparedStatement(sql, preparedStmt, this);
            }
            else
            {
                return preparedStmt;
            }
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException
    {
        transactionRecord(sql);
        try
        {
            PreparedStatement preparedStmt = dbConn.prepareStatement(sql,
                    columnNames);
            if (dbConnPool.isMonitorSwitchOn())
            {
                return new PooledPreparedStatement(sql, preparedStmt, this);
            }
            else
            {
                return preparedStmt;
            }
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException
    {
        transactionRecord(sql);
        try
        {
            PreparedStatement preparedStmt = dbConn.prepareStatement(sql,
                    resultSetType, resultSetConcurrency);
            if (dbConnPool.isMonitorSwitchOn())
            {
                return new PooledPreparedStatement(sql, preparedStmt, this);
            }
            else
            {
                return preparedStmt;
            }
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        transactionRecord(sql);
        try
        {
            PreparedStatement preparedStmt = dbConn.prepareStatement(sql,
                    resultSetType, resultSetConcurrency, resultSetHoldability);
            if (dbConnPool.isMonitorSwitchOn())
            {
                return new PooledPreparedStatement(sql, preparedStmt, this);
            }
            else
            {
                return preparedStmt;
            }
        }
        catch (SQLException ex)
        {
            handleException(sql, ex);
            throw ex;
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        try
        {
            dbConn.releaseSavepoint(savepoint);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void rollback() throws SQLException
    {
        try
        {
            dbConn.rollback();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
        try
        {
            dbConn.rollback(savepoint);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        try
        {
            dbConn.setAutoCommit(autoCommit);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        try
        {
            dbConn.setCatalog(catalog);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setClientInfo(Properties properties)
            throws SQLClientInfoException
    {
        try
        {
            dbConn.setClientInfo(properties);
        }
        catch (SQLClientInfoException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setClientInfo(String name, String value)
            throws SQLClientInfoException
    {
        try
        {
            dbConn.setClientInfo(name, value);
        }
        catch (SQLClientInfoException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException
    {
        try
        {
            dbConn.setHoldability(holdability);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        try
        {
            dbConn.setReadOnly(readOnly);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        try
        {
            return dbConn.setSavepoint();
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
        try
        {
            return dbConn.setSavepoint(name);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
        try
        {
            dbConn.setTransactionIsolation(level);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException
    {
        try
        {
            dbConn.setTypeMap(map);
        }
        catch (SQLException ex)
        {
            handleException(ex);
            throw ex;
        }
    }

}
