package kafeihu.zk.database.pool;

import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.database.DBConstants;

/**
 * Created by zhangkuo on 2016/11/22.
 */
public class DBConnectionPoolConfig {
    // 连接池ID
    private String id;
    // 用户名
    private String username;
    // 密码
    private String password;
    // 数据库Url地址
    private String jdbcUrl;
    // 连接池初始连接数
    private int initialSize = DBConstants.POOL_InitialSize;
    // 连接池最大连接数
    private int maxSize = DBConstants.POOL_MaxSize;
    // 连接池最小连接数
    private int minSize = DBConstants.POOL_MinSize;
    // 连接空闲时间。单位：秒
    private int maxIdleSeconds = DBConstants.POOL_MaxIdleSeconds;
    // 空闲连接扫描线程执行间隔。单位：秒
    private int idleConnScanSpanSeconds = DBConstants.POOL_IdleConnScanSpanSeconds;
    // 获取连接最大等待时间。单位：毫秒
    private long maxWaitTimeoutMills = DBConstants.POOL_MaxWaitTimeoutMills;
    // 连接有效性判断语句。通常为一个查询语句
    private String validationQuerySql;
    // 连接初始化Sql语句：构造新的物理连接后执行
    // private String[] connectionInitSqls;
    // 允许的数据库连接后台构造线程数
    private int maxCreateThreadCount = DBConstants.POOL_MaxCreateThreadCount;
    // 连接是否被占用过长告警阀值。单位：毫秒
    private long accessSpanWarningMills = DBConstants.POOL_ConnAccessSpanWarningMills;
    //数据库监控开关是否打开。默认为打开
    private boolean monitorSwitchOn = true;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getJdbcUrl()
    {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl)
    {
        this.jdbcUrl = jdbcUrl;
    }

    public int getInitialSize()
    {
        return initialSize;
    }

    public void setInitialSize(int initialSize)
    {
        this.initialSize = initialSize;
    }

    public int getMaxIdleSeconds()
    {
        return maxIdleSeconds;
    }

    public void setMaxIdleSeconds(int maxIdleSeconds)
    {
        this.maxIdleSeconds = maxIdleSeconds;
    }

    public String getValidationQuerySql()
    {
        return validationQuerySql;
    }

    public void setValidationQuerySql(String validationQuerySql)
    {
        this.validationQuerySql = validationQuerySql;
    }

    // public String[] getConnectionInitSqls()
    // {
    // return connectionInitSqls;
    // }
    //
    // public void setConnectionInitSqls(String[] connectionInitSqls)
    // {
    // this.connectionInitSqls = connectionInitSqls;
    // }
    public long getMaxWaitTimeoutMills()
    {
        return maxWaitTimeoutMills;
    }

    public void setMaxWaitTimeoutMills(long maxWaitTimeoutMills)
    {
        if (maxWaitTimeoutMills > 0)
        {
            this.maxWaitTimeoutMills = maxWaitTimeoutMills;
        }
    }

    public int getMaxSize()
    {
        return maxSize;
    }

    public void setMaxSize(int maxSize)
    {
        this.maxSize = maxSize;
    }

    public int getMinSize()
    {
        return minSize;
    }

    public void setMinSize(int minSize)
    {
        this.minSize = minSize;
    }

    public int getMaxCreateThreadCount()
    {
        return maxCreateThreadCount;
    }

    public void setMaxCreateThreadCount(int maxCreateThreadCount)
    {
        if (maxCreateThreadCount > 0)
        {
            this.maxCreateThreadCount = maxCreateThreadCount;
        }
    }

    public int getIdleConnScanSpanSeconds()
    {
        return idleConnScanSpanSeconds;
    }

    public void setIdleConnScanSpanSeconds(int idleConnScanSpanSeconds)
    {
        if (idleConnScanSpanSeconds > 0)
        {
            this.idleConnScanSpanSeconds = idleConnScanSpanSeconds;
        }
    }

    public long getAccessSpanWarningMills()
    {
        return accessSpanWarningMills;
    }

    public void setAccessSpanWarningMills(long accessSpanWarningMills)
    {
        if (accessSpanWarningMills > 0)
        {
            this.accessSpanWarningMills = accessSpanWarningMills;
        }
    }

    public boolean isMonitorSwitchOn()
    {
        return monitorSwitchOn;
    }

    public void setMonitorSwitchOn(boolean monitorSwitchOn)
    {
        this.monitorSwitchOn = monitorSwitchOn;
    }

    /**
     * 检查配置数据是否合法有效
     *
     * @throws Exception
     */
    public void checkConfigData() throws Exception
    {
        String clazzName = getClass().getName();
        if (MiscUtil.isEmpty(id))
        {
            throw new Exception(clazzName + ":illegal id");
        }
        if (MiscUtil.isEmpty(username))
        {
            throw new Exception(clazzName + ":illegal username");
        }
        if (MiscUtil.isEmpty(jdbcUrl))
        {
            throw new Exception(clazzName + ":illegal jdbcUrl");
        }
        if (minSize < 0)
        {
            throw new Exception(clazzName + ":illegal minSize");
        }
        if (maxSize < 1)
        {
            throw new Exception(clazzName + ":illegal maxSize");
        }
        if (minSize > maxSize)
        {
            throw new Exception(clazzName + ":illegal maxSize/minSize pair");
        }
        if (initialSize > maxSize)
        {
            throw new Exception(clazzName + ":illegal maxSize/initialSize pair");
        }
        if (initialSize < minSize)
        {
            throw new Exception(clazzName + ":illegal minSize/initialSize pair");
        }
        if (maxIdleSeconds < 0)
        {
            throw new Exception(clazzName + ":illegal maxIdleSeconds");
        }
        if (idleConnScanSpanSeconds < 0)
        {
            throw new Exception(clazzName + ":illegal idleConnScanSpanSeconds");
        }
        if (maxWaitTimeoutMills < 0)
        {
            throw new Exception(clazzName + ":illegal maxWaitTimeoutMills");
        }
        if (maxCreateThreadCount > (maxSize - minSize))
        {
            throw new Exception(clazzName + ":illegal maxCreateThreadCount");
        }
        if (MiscUtil.isEmpty(validationQuerySql))
        {
            throw new Exception(clazzName
                    + ":validationQuerySql can not be empty");
        }
    }

    public static DBConnectionPoolConfig parseXmlConfig(String xmlPoolConfig)
            throws Exception
    {
        DBConnectionPoolConfig poolConfigObj = new DBConnectionPoolConfig();
        poolConfigObj.setId(XmlUtil.getXmlElement("id", xmlPoolConfig));
        poolConfigObj.setUsername(XmlUtil.getXmlElement("username",
                xmlPoolConfig));
        poolConfigObj.setPassword(XmlUtil.getXmlElement("password",
                xmlPoolConfig));
        poolConfigObj.setJdbcUrl(XmlUtil
                .getXmlElement("jdbcUrl", xmlPoolConfig));
        poolConfigObj.setValidationQuerySql(XmlUtil.getXmlElement(
                "validationQuerySql", xmlPoolConfig));

        String tmpValue = "";
        tmpValue = XmlUtil.getXmlElement("maxSize", xmlPoolConfig);
        int maxSize = MiscUtil.parseInt(tmpValue, DBConstants.POOL_MaxSize);
        poolConfigObj.setMaxSize(maxSize);

        tmpValue = XmlUtil.getXmlElement("initialSize", xmlPoolConfig);
        int initialSize = MiscUtil.parseInt(tmpValue, DBConstants.POOL_InitialSize);
        poolConfigObj.setInitialSize(initialSize);

        tmpValue = XmlUtil.getXmlElement("minSize", xmlPoolConfig);
        int minSize = MiscUtil.parseInt(tmpValue, DBConstants.POOL_MinSize);
        poolConfigObj.setMinSize(minSize);

        tmpValue = XmlUtil.getXmlElement("maxIdleSeconds", xmlPoolConfig);
        int maxIdleSeconds = MiscUtil.parseInt(tmpValue, DBConstants.POOL_MaxIdleSeconds);
        poolConfigObj.setMaxIdleSeconds(maxIdleSeconds);

        tmpValue = XmlUtil.getXmlElement("idleConnScanSpanSeconds",
                xmlPoolConfig);
        int idleConnScanSpanSeconds = MiscUtil.parseInt(tmpValue, DBConstants.POOL_IdleConnScanSpanSeconds);
        poolConfigObj.setIdleConnScanSpanSeconds(idleConnScanSpanSeconds);

        tmpValue = XmlUtil.getXmlElement("maxWaitTimeoutMills", xmlPoolConfig);
        long maxWaitTimeoutMills = MiscUtil.parseLong(tmpValue, DBConstants.POOL_MaxWaitTimeoutMills);
        poolConfigObj.setMaxWaitTimeoutMills(maxWaitTimeoutMills);

        tmpValue = XmlUtil.getXmlElement("maxCreateThreadCount", xmlPoolConfig);
        int maxCreateThreadCount = MiscUtil.parseInt(tmpValue, DBConstants.POOL_MaxCreateThreadCount);
        poolConfigObj.setMaxCreateThreadCount(maxCreateThreadCount);

        tmpValue = XmlUtil.getXmlElement("accessSpanWarningMills", xmlPoolConfig);
        long accessSpanWarningMills = MiscUtil.parseLong(tmpValue, DBConstants.POOL_ConnAccessSpanWarningMills);
        poolConfigObj.setAccessSpanWarningMills(accessSpanWarningMills);

        tmpValue = XmlUtil.getXmlElement("monitorSwitchOn", xmlPoolConfig,"Y");
        poolConfigObj.setMonitorSwitchOn(!tmpValue.equalsIgnoreCase("N"));

        poolConfigObj.checkConfigData();
        return poolConfigObj;
    }
}
