package kafeihu.zk.bserver.schedule;

import kafeihu.zk.base.schedule.Scheduler;
import kafeihu.zk.base.schedule.Task;
import kafeihu.zk.base.schedule.policy.BasePolicy;
import kafeihu.zk.base.schedule.policy.Policy;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.bserver.manager.LoggerManager;
import kafeihu.zk.bserver.manager.ModuleManager;
import kafeihu.zk.bserver.service.ServiceManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 任务调度管理器：在系统启动时，按配置进行任务调度
 *
 * Created by zhangkuo on 2017/5/30.
 */
public class ScheduleManager extends ServiceManager {
    /**
     * 配置文件名
     */
    private final static String Config_File_Name = "schedule-config.xml";

    /**
     * 调度器
     */
    private final static Scheduler m_scheduler = Scheduler.getDefaultScheduler();

    private ScheduleManager()
    {
        super();
    }

    /**
     * 启动服务
     */
    public static synchronized void startService() throws Exception
    {
        if (ResourceUtil.isSysDataResourceExists(Config_File_Name))
        {
            String sysTaskConfigData = ResourceUtil.getSysDataResourceContent(Config_File_Name);
            // 调度应用级任务
            try
            {
                scheduleTask(sysTaskConfigData, null);
            }
            catch (Exception exp)
            {
                throw new Exception("schedule System task failed. exp:" + exp.getMessage());
            }
        }
        // 调度模块级任务
        List<String> moduleNameList = ModuleManager.getModuleName();
        for (String moduleName : moduleNameList)
        {
            if (ResourceUtil.isModuleDataResourceExists(moduleName, Config_File_Name))
            {
                try
                {
                    String moduleTaskConfigData = ResourceUtil.getModuleDataResourceContent(
                            moduleName, Config_File_Name);
                    scheduleTask(moduleTaskConfigData, moduleName);
                }
                catch (Exception exp)
                {
                    throw new Exception("schedule Module task failed, moduleName:" + moduleName
                            + " . exp:" + exp.getMessage());
                }
            }
        }
    }

    private static void scheduleTask(String configData, String moduleName) throws Exception
    {
        // 解析调度任务
        Map<String, String> taskXmlConfigMap = new ConcurrentHashMap<String, String>();
        List<String> listTaskConfig = XmlUtil.getAllXmlElements("task", configData);
        for (String xmlTaskConfig : listTaskConfig)
        {
            String name = XmlUtil.getXmlElement("name", xmlTaskConfig);
            if (MiscUtil.isEmpty(name))
            {
                throw new Exception("task/name can not be empty");
            }
            if (taskXmlConfigMap.containsKey(name))
            {
                throw new Exception("duplicate task defined with same name=" + name);
            }
            String impl = XmlUtil.getXmlElement("impl", xmlTaskConfig);
            if (MiscUtil.isEmpty(impl))
            {
                throw new Exception("task/impl can not be empty");
            }
            taskXmlConfigMap.put(name, xmlTaskConfig);
        }

        // 解析调度策略
        Map<String, String> policyXmlConfigMap = new ConcurrentHashMap<String, String>();
        List<String> listPolicyConfig = XmlUtil.getAllXmlElements("policy", configData);
        for (String xmlPolicyConfig : listPolicyConfig)
        {
            String id = XmlUtil.getXmlElement("id", xmlPolicyConfig);
            if (MiscUtil.isEmpty(id))
            {
                throw new Exception("policy/id can not be empty");
            }
            if (policyXmlConfigMap.containsKey(id))
            {
                throw new Exception("duplicate policy defined with same id=" + id);
            }
            String impl = XmlUtil.getXmlElement("impl", xmlPolicyConfig);
            if (MiscUtil.isEmpty(impl))
            {
                throw new Exception("policy/impl can not be empty");
            }
            policyXmlConfigMap.put(id, xmlPolicyConfig);
        }
        // 解析任务与策略绑定关系
        List<String> listScheduleBindingConfig = XmlUtil.getAllXmlElements("scheduleBinding",
                configData);
        for (String scheduleBindingConfig : listScheduleBindingConfig)
        {
            String[] taskNameArray = XmlUtil.getXmlElement("taskName", scheduleBindingConfig)
                    .split(",");
            //taskName不能为空
            if( (taskNameArray.length == 1)&&MiscUtil.isEmpty(taskNameArray[0].trim()))
            {
                throw new Exception("scheduleBinding/taskName can not be empty");
            }
            String[] policyIdArray = XmlUtil.getXmlElement("policyId", scheduleBindingConfig)
                    .split(",");
            //policyId为空，表示无执行策略
            if ((policyIdArray.length == 1)&&MiscUtil.isEmpty(policyIdArray[0].trim()))
            {
                continue;
            }
            for (String taskName : taskNameArray)
            {
                if(MiscUtil.isEmpty(taskName.trim()))
                {
                    continue;
                }
                String taskConfig = taskXmlConfigMap.get(taskName.trim());
                if (null == taskConfig)
                {
                    throw new Exception("no task defined with name=" + taskName);
                }
                for (String policyId : policyIdArray)
                {
                    if(MiscUtil.isEmpty(policyId.trim()))
                    {
                        continue;
                    }
                    String policyConfig = policyXmlConfigMap.get(policyId.trim());
                    if (null == policyConfig)
                    {
                        throw new Exception("no policy defined with id=" + policyId);
                    }
                    // 构造任务实例
                    Task task = createTask(taskConfig, moduleName);
                    // 构造调度策略实例
                    Policy policy = createPolicy(policyConfig);

                    if (!m_scheduler.scheduleTask(task, policy))
                    {
                        throw new Exception("schedule task failed. taskName=" + taskName
                                + ". policyId=" + policyId);
                    }
                    LoggerManager.getSysLogger().info(task.getClass().getName(), "TaskName:"+taskName+" PolicyId:"+policyId+" ModuleName:"+moduleName);
                }
            }
        }
    }

    /**
     * 根据调度策略配置，构造调度策略实例
     *
     * @param policyConfig
     * @return
     * @throws Exception
     */
    private static Policy createPolicy(String policyConfig) throws Exception
    {
        String id = XmlUtil.getXmlElement("id", policyConfig);
        if (MiscUtil.isEmpty(id))
        {
            throw new Exception("policy/id can not be empty");
        }
        String impl = XmlUtil.getXmlElement("impl", policyConfig);
        if (MiscUtil.isEmpty(impl))
        {
            throw new Exception("policy/impl can not be empty");
        }

        int repeatCount = 0;
        String tmpValue = "";
        try
        {
            tmpValue = XmlUtil.getXmlElement("repeatCount", policyConfig, "0");
            repeatCount = Integer.valueOf(tmpValue);
        }
        catch (NumberFormatException exp)
        {
            throw new Exception("illegal policy/repeatCount value:" + tmpValue);
        }

        Date endDate = null;
        try
        {
            tmpValue = XmlUtil.getXmlElement("endDate", policyConfig);
            if (!MiscUtil.isEmpty(tmpValue))
            {
                if (tmpValue.length() == 8)
                {
                    tmpValue += " 00:00:00";
                }
                endDate = MiscUtil.toDate_2(tmpValue);
            }
        }
        catch (ParseException exp)
        {
            throw new Exception("illegal policy/endDate value:" + tmpValue);
        }

        try
        {
            Class<?> taskClazz = Class.forName(impl);
            Constructor<?> constructor = taskClazz.getConstructor(Properties.class);
            Properties prop = XmlUtil.parseProperties(policyConfig);
            BasePolicy policy = (BasePolicy) constructor.newInstance(prop);
            policy.setRepeatCount(repeatCount);
            policy.setEndDate(endDate);
            return policy;
        }
        catch (InvocationTargetException exp)
        {
            throw new Exception(exp.getTargetException());
        }
    }

    /**
     * 根据调度任务配置，构造任务实例
     *
     * @param taskConfig
     * @return
     * @throws Exception
     */
    private static Task createTask(String taskConfig, String moduleName) throws Exception
    {
        String name = XmlUtil.getXmlElement("name", taskConfig);
        if (MiscUtil.isEmpty(name))
        {
            throw new Exception("task/name can not be empty");
        }
        String impl = XmlUtil.getXmlElement("impl", taskConfig);
        if (MiscUtil.isEmpty(impl))
        {
            throw new Exception("task/impl can not be empty");
        }
        try
        {
            Class<?> taskClazz = Class.forName(impl);
            Task task;
            if (null == moduleName)
            {
                Constructor<?> constructor = taskClazz.getConstructor(String.class);
                task = (Task) constructor.newInstance(name);
            }
            else
            {
                Constructor<?> constructor = taskClazz.getConstructor(String.class, String.class);
                task = (ModuleTask) constructor.newInstance(name, moduleName);
            }
            String params = XmlUtil.getXmlElement("params", taskConfig);
            task.setParam(XmlUtil.parseProperties(params));
            return task;
        }
        catch (InvocationTargetException exp)
        {
            throw new Exception(exp.getTargetException());
        }
    }

    /**
     * 停止服务
     */
    public static synchronized void stopService() throws Exception
    {
        // 取消所有任务调度
        m_scheduler.unscheduleTask();
    }

}
