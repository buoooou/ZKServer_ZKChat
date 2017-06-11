package kafeihu.zk.bserver.manager;


import kafeihu.zk.base.pool.ObjectPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
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
    private static void initialize() throws Exception
    {}

}
