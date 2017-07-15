package kafeihu.zk.bserver.manager;


import kafeihu.zk.base.logging.Logger;
import kafeihu.zk.base.pool.ObjectPool;
import kafeihu.zk.base.pool.ObjectPoolConfig;
import kafeihu.zk.base.pool.PoolableObjectFactory;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.bserver.statistics.StatisticsManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 资源池管理器
 *
 * Created by zhangkuo on 2017/6/11.
 */
public class ObjectPoolManager {

    /**
     * 配置文件
     */
    private final static String Config_File_Name = "pool-config.xml";
    /**
     * 系统对象资源池
     */
    private final static Map<String, ObjectPool> m_sysObjectPoolMap = new ConcurrentHashMap<String, ObjectPool>(
            20, 0.8f, 1);

    /**
     * 模块对象资源池
     */
    private final static Map<String, Map<String, ObjectPool>> m_moduleObjectPoolMap = new ConcurrentHashMap<String, Map<String, ObjectPool>>();

    static
    {
        try
        {
            System.out.print("Initializing ObjectPoolManager...... ");
            initialize();
            System.out.println("OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(ObjectPoolManager.class.getName()
                    + ".initialize(). " + exp);
        }
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    private static void initialize() throws Exception {
        if (ResourceUtil.isSysDataResourceExists(Config_File_Name))
        {
            String sysPoolConfigData = ResourceUtil.getSysDataResourceContent(Config_File_Name);
            try
            {
                // 解析加载驱动程序
                String driverConfigData = XmlUtil.getXmlElement("driver-config", sysPoolConfigData);
                List<String> listDriverClass = XmlUtil
                        .getAllXmlElements("driver", driverConfigData);
                loadDriver(listDriverClass);
                // 解析系统级资源对象池
                List<String> listSysObjectPoolConfig = XmlUtil.getAllXmlElements("pool",
                        sysPoolConfigData);
                initObjectPool(listSysObjectPoolConfig, m_sysObjectPoolMap, "");
            }
            catch (Exception exp)
            {
                throw new Exception("initialize System ObjectPool failed. exp:" + exp.getMessage());
            }
        }
        // 解析模块级资源对象池
        List<String> moduleNameList = ModuleManager.getModuleName();
        for (String moduleName : moduleNameList)
        {
            m_moduleObjectPoolMap.remove(moduleName);
            if (ResourceUtil.isModuleDataResourceExists(moduleName, Config_File_Name))
            {
                Map<String, ObjectPool> moduleObjectPool = new ConcurrentHashMap<String, ObjectPool>();

                String modulePoolConfigData = ResourceUtil.getModuleDataResourceContent(
                        moduleName, Config_File_Name);
                try
                {
                    // 解析加载驱动程序
                    String driverConfigData = XmlUtil.getXmlElement("driver-config",
                            modulePoolConfigData);
                    List<String> listModDriverClass = XmlUtil.getAllXmlElements("driver",
                            driverConfigData);
                    loadDriver(listModDriverClass);
                    // 解析模块级资源对象池
                    List<String> listModuleObjectPoolConfig = XmlUtil.getAllXmlElements("pool",
                            modulePoolConfigData);
                    initObjectPool(listModuleObjectPoolConfig, moduleObjectPool, moduleName);
                }
                catch (Exception exp)
                {
                    throw new Exception("initialize Module ObjectPool failed, moduleName:"
                            + moduleName + " . exp:" + exp.getMessage());
                }
                m_moduleObjectPoolMap.put(moduleName, moduleObjectPool);
            }
        }
    }
    private static void loadDriver(List<String> listDriverClass) throws ClassNotFoundException {
        for (String driverClassName : listDriverClass)
        {
            System.out.println("loaddriver:"+driverClassName);
            Class.forName(driverClassName);
        }
    }
    private static void initObjectPool(List<String> listPoolConfig,
                                       Map<String, ObjectPool> objectPoolMap, String moduleName) throws Exception {
        for (String poolConfig : listPoolConfig)
        {
            String id = XmlUtil.getXmlElement("id", poolConfig);
            if (MiscUtil.isEmpty(id))
            {
                throw new Exception("pool/id can not be empty");
            }
            if (objectPoolMap.containsKey(id))
            {
                throw new Exception(" duplicate ObjectPool defined with id=" + id);
            }
            ObjectPool poolObject = createObjectPool(id, poolConfig);
            String alias = XmlUtil.getXmlElement("alias", poolConfig, id);

//            ObjectPoolStatWrapper statObj = new ObjectPoolStatWrapper(poolObject, alias, id,
//                    moduleName);
//            StatisticsManager.register(statObj);

            objectPoolMap.put(id, poolObject);
            //写日志
            LoggerManager.getSysLogger().info(poolObject.getClass().getName()+" Id:"+id,poolObject);
        }
    }
    /**
     * 根据配置文件，构造对象资源池
     *
     * @param id
     * @param poolConfig
     * @throws Exception
     */
    private static ObjectPool createObjectPool(String id, String poolConfig) throws Exception {
        try
        {
            String poolImpl = XmlUtil.getXmlElement("poolImpl", poolConfig);
            if (MiscUtil.isEmpty(poolImpl))
            {
                throw new Exception("pool/poolImpl can not be empty.");
            }
            String poolableObjectFactoryConfig = XmlUtil.getXmlElement("poolableObjectFactory",
                    poolConfig);
            PoolableObjectFactory factory = createPoolableObjectFactory(poolableObjectFactoryConfig);
            Class<?> cls = Class.forName(poolImpl);
            Constructor<?> constructor = cls.getConstructor(PoolableObjectFactory.class,
                    ObjectPoolConfig.class);
            String poolParam = XmlUtil.getXmlElement("poolParam", poolConfig);
            ObjectPoolConfig poolConfigObj = createObjectPoolConfig(poolParam);
            try
            {
                ObjectPool poolObj = (ObjectPool) constructor.newInstance(factory, poolConfigObj);

                return poolObj;
            }
            catch (InvocationTargetException exp)
            {
                throw new Exception(exp.getTargetException());
            }
        }
        catch (Exception exp)
        {
            throw new Exception(exp.getMessage() + " . pool/id=" + id);
        }
    }
    /**
     * 构造对象池配置对象
     *
     * @param poolConfig
     * @return
     * @throws Exception
     */
    private static ObjectPoolConfig createObjectPoolConfig(String poolConfig) throws Exception
    {
        ObjectPoolConfig poolConfigObj = new ObjectPoolConfig();
        String tmpValue = "";
        try
        {
            tmpValue = XmlUtil.getXmlElement("coreSize", poolConfig);

            int coreSize = Integer.parseInt(tmpValue);

            tmpValue = XmlUtil.getXmlElement("maxSize", poolConfig, tmpValue);
            int maxSize = Integer.parseInt(tmpValue);

            poolConfigObj.setCoreSize(coreSize);
            poolConfigObj.setMaxSize(maxSize);

            tmpValue = XmlUtil.getXmlElement("keepAliveSeconds", poolConfig, "600");
            poolConfigObj.setKeepAliveSeconds(Integer.parseInt(tmpValue));

            tmpValue = XmlUtil.getXmlElement("borrowWaitMills", poolConfig, "0");
            poolConfigObj.setBorrowWaitMills(Integer.parseInt(tmpValue));

            tmpValue = XmlUtil.getXmlElement("keepPoolableObjectActive", poolConfig, "N");
            poolConfigObj.setKeepPoolableObjectActive(tmpValue.equalsIgnoreCase("Y"));

            tmpValue = XmlUtil.getXmlElement("activateObjectIntervalSeconds", poolConfig, "10");
            poolConfigObj.setActivateObjectIntervalSeconds(Integer.valueOf(tmpValue));

            tmpValue = XmlUtil.getXmlElement("activateObjectLoopCnt", poolConfig, "1");
            poolConfigObj.setActivateObjectLoopCnt(Integer.valueOf(tmpValue));
        }
        catch (Exception e)
        {
            throw new Exception(
                    "illegal pool/poolParam/coreSize|timeoutMills|activateObjectIntervalSeconds|activateObjectLoopCnt value:"
                            + tmpValue);
        }

        if (poolConfigObj.getCoreSize() <= 0)
        {
            throw new Exception("pool/poolParam/coreSize must be greater than zero");
        }
        return poolConfigObj;
    }

    /**
     * 构造对象工厂
     *
     * @param factoryConfig
     * @return
     * @throws Exception
     */
    private static PoolableObjectFactory createPoolableObjectFactory(String factoryConfig)
            throws Exception
    {
        String factoryImpl = XmlUtil.getXmlElement("factoryImpl", factoryConfig);
        if (MiscUtil.isEmpty(factoryImpl))
        {
            throw new Exception("pool/poolableObjectFactory/factoryImpl can not be empty");
        }

        Class<?> cls = Class.forName(factoryImpl);
        Constructor<?> constructor = cls.getConstructor(String.class);
        String poolableObjectConfig = XmlUtil.getXmlElement("poolableObject", factoryConfig);
        try
        {
            PoolableObjectFactory factory = (PoolableObjectFactory) constructor
                    .newInstance(poolableObjectConfig);
            return factory;
        }
        catch (InvocationTargetException exp)
        {
            throw new Exception(exp.getTargetException());
        }
    }

    /**
     * 获取指定ID的系统级资源池。如果没有相关资源池定义，返回null
     *
     * @param poolId
     * @return
     */
    public static ObjectPool getSysObjectPool(String poolId) throws Exception
    {
        ObjectPool poolObj = m_sysObjectPoolMap.get(poolId);
        if(null == poolObj)
        {
            throw new Exception("no ObjectPool found. poolId:"+poolId);
        }
        else
        {
            return poolObj;
        }
    }

    /**
     * 获取指定模块及ID的模块级资源对象池。如果模块级资源池未定义，返回相同ID的系统级资源池，如果还未找到，返回null
     *
     * @param moduleName
     * @param poolId
     * @return
     */
    public static ObjectPool getModuleObjectPool(String moduleName, String poolId) throws Exception
    {
        ObjectPool poolObj = null;
        Map<String, ObjectPool> moduleObjectPoolMap = m_moduleObjectPoolMap.get(moduleName);
        if (null != moduleObjectPoolMap)
        {
            poolObj = moduleObjectPoolMap.get(poolId);
        }
        if (null == poolObj)
        {
            poolObj = m_sysObjectPoolMap.get(poolId);
        }
        if(null == poolObj)
        {
            throw new Exception("no ObjectPool found. poolId:"+poolId);
        }
        else
        {
            return poolObj;
        }
    }

    /**
     * 停止所有对象池
     */
    public static void stopObjectPool()
    {
        stopObjectPool(m_sysObjectPoolMap, LoggerManager.getSysLogger());

        Set<String> moduleNameSet = m_moduleObjectPoolMap.keySet();
        for (String moduleName : moduleNameSet)
        {
            Map<String, ObjectPool> moduleObjectPool = m_moduleObjectPoolMap.get(moduleName);
            Logger logger = LoggerManager.getModuleLogger(moduleName);
            stopObjectPool(moduleObjectPool, logger);
        }
    }

    private static void stopObjectPool(Map<String, ObjectPool> objectPoolMap, Logger logger)
    {
        Collection<ObjectPool> objectPools = objectPoolMap.values();
        for (ObjectPool objectPool : objectPools)
        {
            try
            {
                objectPool.close();
                logger.info(objectPool.getClass().getName(), "closed. " + objectPool);
            }
            catch (Exception exp)
            {
                logger.error(objectPool.getClass().getName(), "stopObjectPool exception : " + exp);
            }
        }
        objectPoolMap.clear();
    }

}
