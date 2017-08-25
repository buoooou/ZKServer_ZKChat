package kafeihu.zk.bserver.core;

import kafeihu.zk.bserver.core.config.GlobalConfig;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.bserver.manager.ContextManager;
import kafeihu.zk.bserver.manager.LoggerManager;
import kafeihu.zk.bserver.manager.ModuleManager;
import kafeihu.zk.bserver.service.ServiceManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public class BserverManager {

    private final static String Config_File_Name = "bserver-config.xml";
    private final static Map<String, Class<?>> m_serviceManagers = new HashMap<String, Class<?>>();
    private final static BlockingDeque<Runnable> startQueue = new LinkedBlockingDeque<>();
    private final static int corePoolSize = 2;
    private final static int maximumPoolSize = 5;
    private final static long keepAliveTime = 10;
    private final static TimeUnit unit = TimeUnit.SECONDS;
    private final static ThreadPoolExecutor startExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, startQueue);
    /**
     * 服务启动时间
     */
    private static long m_startUpTime = System.currentTimeMillis();

    /**
     * 预加载并初始化静态类
     *
     * @param preloadConfig
     * @throws Exception
     */
    private static void preLoadClass(String preloadConfig) throws Exception {
        List<String> listPreloadClass = XmlUtil.getAllXmlElements("preLoadClass", preloadConfig);
        for (String preloadClass : listPreloadClass) {
            if (!MiscUtil.isEmpty(preloadClass)) {
                Class.forName(preloadClass);
            }
        }
    }

    /**
     * 注册系统关闭挂钩
     *
     * @param hookConfig
     * @throws Exception
     */
    private static void registerShutdownHooks(String hookConfig) throws Exception {
        List<String> listHookThread = XmlUtil.getAllXmlElements("hookThread", hookConfig);
        for (String hookThreadConfig : listHookThread) {
            String impl = XmlUtil.getXmlElement("impl", hookThreadConfig);
            if (!MiscUtil.isEmpty(impl)) {
                Thread hookThread = null;
                try {
                    Class<?> hookClazz = Class.forName(impl);
                    hookThread = (Thread) hookClazz.newInstance();
                    Method setParamMethod = hookClazz.getMethod("setParams", String.class);
                    setParamMethod.invoke(hookThread, hookThreadConfig);
                } catch (NoSuchMethodException nsme) {
                }
                if (null != hookThread) {
                    Runtime.getRuntime().addShutdownHook(hookThread);
                }
            }
        }
    }

    /**
     * 服务启动
     */
    public static void start() throws Exception {
        LoggerManager.getSysLogger().info("UniBServer", "System starting......");
        String configData = ResourceUtil.getSysDataResourceContent(Config_File_Name);

        // 初始化全局配置
        String globalConfig = XmlUtil.getXmlElement("global", configData);
        GlobalConfig.init(globalConfig);
        LoggerManager.getSysLogger().info("UniBServer", "Init global config OK!");

        // 预加载工具类
        String preloadConfig = XmlUtil.getXmlElement("preLoadClasses", configData);
        preLoadClass(preloadConfig);
        LoggerManager.getSysLogger().info("UniBServer", "PreLoad classes OK!");

        // 注册系统关闭挂钩
        String hookConfig = XmlUtil.getXmlElement("shutdownHooks", configData);
        registerShutdownHooks(hookConfig);
        LoggerManager.getSysLogger().info("UniBServer", "RegisterShutdownHooks OK!");


        // 启动系统服务管理器
        String serviceConfig = XmlUtil.getXmlElement("serviceManagers", configData);
        startServiceManager(serviceConfig);
        LoggerManager.getSysLogger().info("UniBServer", "StartServiceManager OK!");

        // 活跃模块名记日志
        logActiveModule();

        LoggerManager.getSysLogger().info(ContextManager.getApplicationContext().getApplicationName(), "System started" + System.getProperty("line.separator"));
        // 写启动日志到文件
        LoggerManager.getSysLogger().flush();

        m_startUpTime = System.currentTimeMillis();

    }

    /**
     * 启动BServer相关服务管理器
     *
     * @param serviceConfig
     * @throws Exception
     */
    private static void startServiceManager(String serviceConfig) throws Exception {

        List<Future<Void>> results = new ArrayList<>();
        List<String> listService = XmlUtil.getAllXmlElements("serviceManager", serviceConfig);
        for (int i = 0; i < listService.size(); i++) {
            String serviceConfigData = (String) listService.get(i);
            results.add(startExecutor.submit(new StartService(serviceConfigData)));
        }
        boolean fail = false;
        for (Future<Void> result : results) {
            try {
                result.get();
            } catch (Exception e) {
                fail = true;
                LoggerManager.getSysLogger().error("StatService failed! Reason: ", e.getMessage());
            }

        }
        if (fail) {
            throw new Exception("StatService failed!");
        }
        Log4JManager.getConsoleLogger().info("StatService OK!");
    }

    private static class StartService implements Callable<Void> {

        private String serviceConfigData = "";

        public StartService(String serviceConfig) {
            this.serviceConfigData = serviceConfig;
        }

        @Override
        public Void call() throws Exception {
            List<String> serviceManagerList = new ArrayList<String>();

            String name = XmlUtil.getXmlElement("name", serviceConfigData);
            if (serviceManagerList.contains(name)) {
                throw new Exception("duplicate serviceManager defined with name=" + name);
            }
            String description = XmlUtil.getXmlElement("description", serviceConfigData);
            if (MiscUtil.isEmpty(description)) {
                description = name;
            }
            String serviceManagerName = XmlUtil.getXmlElement("class", serviceConfigData);
            serviceManagerList.add(serviceManagerName);

            Class<?> serviceManager = Class.forName(serviceManagerName);
            // 判断是否是ServiceManager子类
            if (!ServiceManager.class.isAssignableFrom(serviceManager)) {
                throw new Exception(serviceManagerName + " is not type of "
                        + ServiceManager.class.getName());
            }
            // 启动服务
            Log4JManager.getConsoleLogger().info("Starting service " + description + "...... ");
            Method startServiceMethod = serviceManager.getMethod("startService");
            try {
                startServiceMethod.invoke(serviceManager);
            } catch (InvocationTargetException exp) {
                throw new Exception(exp.getTargetException().getMessage());
            }
            m_serviceManagers.put(name, serviceManager);

            return null;
        }
    }

    /**
     * 停止BServer服务管理器
     */
    private static void stopServiceManager() {
        Collection<Class<?>> serviceManagerCls = m_serviceManagers.values();
        for (Class<?> serviceManagerClazz : serviceManagerCls) {
            try {
                Method stopServiceMethod = serviceManagerClazz.getMethod("stopService");
                stopServiceMethod.invoke(serviceManagerClazz);
            } catch (Exception exp) {
            }
        }
    }

    private static void logActiveModule() {
        StringBuilder sb = new StringBuilder();
        List<String> activeModuleName = ModuleManager.getModuleName();
        for (String moduleName : activeModuleName) {
            sb.append(moduleName).append(", ");
        }
        String activeModules = sb.toString();
        int index = activeModules.lastIndexOf(",");
        if (index > 0) {
            activeModules = activeModules.substring(0, index);
        }
        Log4JManager.getConsoleLogger().info("Active module: " + activeModules);
        LoggerManager.getSysLogger().info("Active module: ", activeModules);

    }

    /**
     * 返回网关服务启动时间
     *
     * @return
     */
    public static long getStartTime() {
        return m_startUpTime;
    }

    /**
     * 停止BServer服务
     */
    public static void stop() {
        // 停止服务管理器
        stopServiceManager();
    }
}
