package kafeihu.zk.bserver.manager;

import kafeihu.zk.bserver.core.context.ApplicationContext;
import kafeihu.zk.bserver.core.context.ModuleContext;
import kafeihu.zk.bserver.core.exception.UndefinedModuleException;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 应用程序上下文。应用启动后，通过该类获取应用相关信息
 * Created by zhangkuo on 2016/11/24.
 */
public final class ContextManager {
    /**
     * 配置文件名
     */
    private final static String Config_File_Name = "module-config.xml";

    /**
     * 应用上下文实例
     */
    private static ApplicationContext m_applicationContext;

    /**
     * 模块上下文实例
     */
    private static Map<String, ModuleContext> m_activeModuleContextMap = new ConcurrentHashMap<String, ModuleContext>();

    static
    {
        try
        {
            Log4JManager.getConsoleLogger().info("Initializing ContextManager......");
            initialize();
            Log4JManager.getConsoleLogger().info("Initializing ContextManager OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(ContextManager.class.getName()
                    + ".initialize().  " + exp);
        }
    }

    /**
     * 初始化上下文管理器
     *
     * @throws Exception
     */
    private static void initialize() throws Exception
    {
        String configData = ResourceUtil.getSysDataResourceContent(Config_File_Name);
        // 初始化应用上下文实例
        String appConfig = XmlUtil.getXmlElement("application", configData);

        if (MiscUtil.isEmpty(appConfig))
        {
            throw new Exception("missing application configuration");
        }
        initApplicationContext(appConfig);
        // 初始化模块上下文
        List<String> moduleConfigList = XmlUtil.getAllXmlElements("module", configData);
        initModuleContext(moduleConfigList);
    }

    /**
     * 初始化应用全局上下文
     *
     * @param appConfig
     * @throws Exception
     */
    private static void initApplicationContext(String appConfig) throws Exception
    {
        String appName = XmlUtil.getXmlElement("name", appConfig, "unibserver");
        String contextImpl = XmlUtil.getXmlElement("contextImpl", appConfig);

        if (MiscUtil.isEmpty(contextImpl))
        {
            throw new Exception("contextImpl of application can not be empty");
        }
        try
        {
            Class<?> contextClazz = Class.forName(contextImpl);
            Constructor<?> constructor = contextClazz.getConstructor(String.class);
            m_applicationContext = (ApplicationContext) constructor.newInstance(appName);
        }

        catch (InvocationTargetException ite)
        {
            Throwable exp = ite.getTargetException();
            throw new Exception(ContextManager.class.getName()
                    + " initialize ApplicationContext failed#1:" + exp);
        }
        catch (Exception exp)
        {
            throw new Exception(ContextManager.class.getName()
                    + " initialize ApplicationContext failed:" + exp);
        }
        try
        {
            m_applicationContext.loadContextData(ResourceUtil.getSysExtDataPath());
        }
        catch (Exception exp)
        {
            throw new Exception(m_applicationContext.getClass().getName()
                    + ".loadContextData failed:" + exp);
        }
    }

    /**
     * 初始化模块上下文
     *
     * @param listModuleConfig
     * @throws Exception
     */
    private static void initModuleContext(List<String> listModuleConfig) throws Exception
    {
        for (String moduleConfig : listModuleConfig)
        {
            String active = XmlUtil.getXmlElement("active", moduleConfig, "Y");
            if (!active.equalsIgnoreCase("Y"))
            {
                continue;
            }
            String moduleName = XmlUtil.getXmlElement("name", moduleConfig);
            if (MiscUtil.isEmpty(moduleName))
            {
                throw new Exception("name of module can not be empty.");
            }
            if (m_activeModuleContextMap.containsKey(moduleName))
            {
                throw new Exception("duplicate module defined with same name : " + moduleName);
            }
            String contextImpl = XmlUtil.getXmlElement("contextImpl", moduleConfig);
            if (MiscUtil.isEmpty(contextImpl))
            {
                throw new Exception("contextImpl of module can not be empty. moduleName:"
                        + moduleName);
            }

            ModuleContext moduleContext = null;
            try
            {
                Class<?> contextClazz = Class.forName(contextImpl);
                Constructor<?> constructor = contextClazz.getConstructor(String.class);
                moduleContext = (ModuleContext) constructor.newInstance(moduleName);
            }
            catch (InvocationTargetException ite)
            {
                Throwable exp = ite.getTargetException();
                throw new Exception(ContextManager.class.getName()
                        + " initialize ModuleContext failed#1:" + exp + " . moduleName:"
                        + moduleName);
            }
            catch (Exception exp)
            {
                throw new Exception(ContextManager.class.getName()
                        + " initialize ModuleContext failed:" + exp + " . moduleName:" + moduleName);
            }
            try
            {
                moduleContext.loadContextData(ResourceUtil.getModuleExtDataPath(moduleName));
                // 保存模块上下文
                m_activeModuleContextMap.put(moduleName, moduleContext);
            }
            catch (Exception exp)
            {
                throw new Exception(moduleContext.getClass().getName() + ".loadContextData failed:"
                        + exp + " . moduleName:" + moduleName);
            }
        }
    }

    /**
     * 获取应用上下文实例
     *
     * @return
     */
    public static ApplicationContext getApplicationContext()
    {
        return m_applicationContext;
    }

    /**
     * 获取指定名称的模块上下文实例
     *
     * @param moduleName
     * @return
     * @throws UndefinedModuleException
     */
    public static ModuleContext getModuleContext(String moduleName) throws UndefinedModuleException
    {
        ModuleContext context = m_activeModuleContextMap.get(moduleName);
        if (null == context)
        {
            throw new UndefinedModuleException(moduleName);
        }
        return context;
    }
}
