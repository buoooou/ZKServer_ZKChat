package kafeihu.zk.manager;

import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.db.model.DBConnectionPoolConfig;
import kafeihu.zk.db.pool.DBConnectionPool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangkuo on 2016/11/22.
 */
public class DBConnectionPoolManager {

    /**
     * 配置文件
     */
    private final static String Config_File_Name = "datasource-config.xml";
    /**
     * 数据库连接池实例容器
     */
    private final static Map<String, DBConnectionPool> m_DBConnPoolInstMap = new ConcurrentHashMap<String, DBConnectionPool>(
            20, 0.8f, 1);

    static
    {
        try
        {
            System.out.print("Initializing DBConnectionPoolManager...... ");
            initialize();
            logPoolInfo();
            System.out.println("OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(
                    DBConnectionPoolManager.class.getName() + ".initialize(). "
                            + exp);
        }
    }

    private static void logPoolInfo()
    {
        Collection<DBConnectionPool> dbConnectionPools = m_DBConnPoolInstMap.values();
        for (DBConnectionPool dbConnectionPool : dbConnectionPools)
        {
            LoggerManager.getSysLogger().info(dbConnectionPool.getClass().getName(), dbConnectionPool.toString());
        }
        Collection<String> keys = m_DBConnPoolInstMap.keySet();
        for (String key : keys)
        {
            LoggerManager.getSysLogger().info("连接名：", key);
        }

    }
    /**
     * 初始化
     *
     * @throws Exception
     */
    private static void initialize() throws Exception
    {
        if (ResourceUtil.isSysDataResourceExists(Config_File_Name))
        {
            String sysPoolConfigData = ResourceUtil
                    .getSysDataResourceContent(Config_File_Name);
            try
            {
                parseDBConnectionPoolCfg("", sysPoolConfigData);
            }
            catch (Exception exp)
            {
                throw new Exception(
                        "initialize System DBConnectionPool failed. exp:"
                                + exp.getMessage());
            }
        }
        // 解析模块级资源对象池
        List<String> moduleNameList = ModuleManager.getModuleName();
        for (String moduleName : moduleNameList)
        {
            if (ResourceUtil.isModuleDataResourceExists(moduleName,
                    Config_File_Name))
            {
                String modulePoolConfigData = ResourceUtil
                        .getModuleDataResourceContent(moduleName,
                                Config_File_Name);
                try
                {
                    parseDBConnectionPoolCfg(moduleName, modulePoolConfigData);
                }
                catch (Exception exp)
                {
                    throw new Exception(
                            "initialize Module DBConnectionPool failed, module:"
                                    + moduleName + " . exp:" + exp.getMessage());
                }
            }
        }
    }

    private static void parseDBConnectionPoolCfg(String moduleName,
                                                 String xmlDBConnPoolConfigData) throws Exception
    {
        // 加载JDBC驱动程序
        String driverConfigData = XmlUtil.getXmlElement("driver-config",
                xmlDBConnPoolConfigData);
        List<String> listDriverClass = XmlUtil.getAllXmlElements("driver",
                driverConfigData);
        for (String driverClassName : listDriverClass)
        {
            Class.forName(driverClassName);
        }

        // 解析并构造数据库连接池实例
        List<String> listDBConnPoolConfig = XmlUtil.getAllXmlElements(
                "dbConnectionPool", xmlDBConnPoolConfigData);

        for (String xmlPoolConfig : listDBConnPoolConfig)
        {

            DBConnectionPool dbConnPoolInst = createDBConnectionPool(
                    moduleName, xmlPoolConfig);
            String id = dbConnPoolInst.getID();
            if (m_DBConnPoolInstMap.containsKey(id))
            {
                throw new Exception(" duplicate dbConnectionPool defined. id:"
                        + id);
            }
            String alias = XmlUtil.getXmlElement("alias", xmlPoolConfig, id);

//            DBConnectionPoolStatWrapper statObj = new DBConnectionPoolStatWrapper(
//                    dbConnPoolInst, alias, id, moduleName);
//            StatisticsManager.register(statObj);

            m_DBConnPoolInstMap.put(id, dbConnPoolInst);
        }
    }

    private static String combinePoolId(String moduleName, String orgId)
    {
        if (MiscUtil.isEmpty(moduleName))
        {
            return orgId;
        }
        return moduleName + "-" + orgId;
    }

    /**
     * 根据配置文件，构造对象资源池
     *
     * @param
     * @param poolConfig
     * @throws Exception
     */
    private static DBConnectionPool createDBConnectionPool(String moduleName,
                                                           String poolConfig) throws Exception
    {
        String id = XmlUtil.getXmlElement("id", poolConfig);
        if (MiscUtil.isEmpty(id))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("create DBConnPool failed. dbConnectionPool/id can not be empty.");
            if (!MiscUtil.isEmpty(moduleName))
            {
                sb.append(" module:").append(moduleName);
            }
            throw new Exception(sb.toString());
        }

        try
        {

            DBConnectionPoolConfig dbConnPoolConfig = DBConnectionPoolConfig
                    .parseXmlConfig(poolConfig);
            dbConnPoolConfig.setId(combinePoolId(moduleName, id));

            DBConnectionPool connPoolObj = new DBConnectionPool(
                    dbConnPoolConfig, LoggerManager.getModuleLogger(moduleName));
            // 连接池使用前必须进行初始化
            connPoolObj.init();
            return connPoolObj;
        }
        catch (Exception exp)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("create DBConnPool failed.");
            sb.append(" id:").append(id);
            if (!MiscUtil.isEmpty(moduleName))
            {
                sb.append(" module:").append(moduleName);
            }
            sb.append(" exp:").append(exp.getMessage());

            throw new Exception(sb.toString());
        }
    }

    /**
     * 获取指定ID的系统级资源池。如果没有相关资源池定义，抛出异常
     *
     * @param poolId
     * @throws Exception
     * @return
     */
    public static DBConnectionPool getSysDBConnectionPool(String poolId)
            throws Exception
    {
        DBConnectionPool poolObj = m_DBConnPoolInstMap.get(poolId);
        if (null == poolObj)
        {
            throw new Exception("not find dbConnectionPool. id:" + poolId);
        }
        return poolObj;
    }

    /**
     * 获取指定模块及ID的模块级资源对象池。如果模块级资源池未定义，返回相同ID的系统级资源池，如果还未找到，抛出异常
     *
     * @param moduleName
     * @param poolId
     * @throws Exception
     * @return
     */
    public static DBConnectionPool getModuleDBConnectionPool(String moduleName,
                                                             String poolId) throws Exception
    {

        if (MiscUtil.isEmpty(moduleName))
        {
            DBConnectionPool poolObj = m_DBConnPoolInstMap.get(poolId);
            if (null == poolObj)
            {
                throw new Exception("no dbConnectionPool found. id:" + poolId);
            }
            return poolObj;
        }

        DBConnectionPool poolObj = m_DBConnPoolInstMap.get(combinePoolId(
                moduleName, poolId));
        if (null == poolObj)
        {
            poolObj = m_DBConnPoolInstMap.get(poolId);
        }
        if (null == poolObj)
        {
            throw new Exception("no dbConnectionPool found. id:" + poolId
                    + " module:" + moduleName);
        }
        return poolObj;
    }

    /**
     * 停止数据库连接池
     */
    public static void stopDBConnectionPool()
    {
        Collection<DBConnectionPool> dbConnectionPools = m_DBConnPoolInstMap
                .values();
        for (DBConnectionPool dbConnPool : dbConnectionPools)
        {
            try
            {
                dbConnPool.close();
                LoggerManager.getSysLogger().info(
                        dbConnPool.getClass().getName(),
                        "closed. " + dbConnPool);
            }
            catch (Exception exp)
            {
                LoggerManager.getSysLogger().error(
                        dbConnPool.getClass().getName(),
                        "stopDBConnectionPool exception : " + exp);
            }
        }
        m_DBConnPoolInstMap.clear();
    }

}
