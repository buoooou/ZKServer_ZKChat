package kafeihu.zk.bserver.statistics;

import kafeihu.zk.db.pool.DBConnectionPool;

/**
 * Created by zhangkuo on 2017/6/11.
 */
public class DBConnectionPoolStatWrapper implements IStatistics {

    /**
     * 要获取统计数据的数据库连接池
     */
    private DBConnectionPool m_pool;
    /**
     * 数据库连接池别名
     */
    private String m_alias;
    /**
     * 数据库连接池ID
     */
    private String m_id;

    /**
     * 模块名称：对于应用级资源，模块名为空
     */
    private String m_moduleName;

    public DBConnectionPoolStatWrapper(DBConnectionPool pool, String alias,String id,String modulename)
    {
        super();
        this.m_pool = pool;
        this.m_alias = alias;
        this.m_id = id;
        this.m_moduleName = modulename;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
        result = prime * result
                + ((m_moduleName == null) ? 0 : m_moduleName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DBConnectionPoolStatWrapper other = (DBConnectionPoolStatWrapper) obj;
        if (m_id == null)
        {
            if (other.m_id != null)
                return false;
        }
        else if (!m_id.equals(other.m_id))
            return false;
        if (m_moduleName == null)
        {
            if (other.m_moduleName != null)
                return false;
        }
        else if (!m_moduleName.equals(other.m_moduleName))
            return false;
        return true;
    }

    @Override
    public Object getStatistics()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<statistics>");
        sb.append("<type>").append(IStatistics.StatType_DBConnPool).append("</type>");
        sb.append("<alias>").append(m_alias).append("</alias>");
        sb.append("<id>").append(m_id).append("</id>");
        sb.append("<module>").append(m_moduleName).append("</module>");
        sb.append("<min>").append(m_pool.getMinSize()).append("</min>");
        sb.append("<max>").append(m_pool.getMaxSize()).append("</max>");
        sb.append("<active>").append(m_pool.getActiveNum()).append("</active>");
        sb.append("<alive>").append(m_pool.getAliveNum()).append("</alive>");
        sb.append("<idle>").append(m_pool.getIdleNum()).append("</idle>");
        sb.append("</statistics>");
        return sb.toString();
    }
}
