package kafeihu.zk.bserver.manager;

import kafeihu.zk.bserver.core.exception.BServerException;
import kafeihu.zk.bserver.core.exception.UndefinedModuleException;
import kafeihu.zk.bserver.core.exception.UndefinedProcException;
import kafeihu.zk.bserver.core.exception.model.ErrorCodeConstants;
import kafeihu.zk.bserver.proc.base.BaseProc;
import kafeihu.zk.bserver.proc.RequestData;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务处理类管理器，负责加载、解析、定位业务处理类
 * Created by zhangkuo on 2016/11/25.
 */
public class ProcManager {
    private final static String Config_File_Name = "proc-config.xml";

    private final static String Scope_Singleton = "singleton";
    private final static String Scope_Prototype = "prototype";

    private final static Map<String, Map<String, BaseProc>> m_procInstMap = new ConcurrentHashMap<String, Map<String, BaseProc>>();
    private final static Map<String, Map<String, ProcMetaData>> m_procMetaDataMap = new ConcurrentHashMap<String, Map<String, ProcMetaData>>();

    static
    {
        try
        {
            System.out.print("Initializing ProcManager...... ");
            initialize();
            System.out.println("OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(ProcManager.class.getName() + ".initialize().  "
                    + exp);
        }
    }

    /**
     * 初始化业务处理类
     *
     * @throws Exception
     */
    private static void initialize() throws Exception
    {
        List<String> moduleNameList = ModuleManager.getModuleName();
        for (String moduleName : moduleNameList)
        {
            initModuleProc(moduleName);
        }
    }

    /**
     * 初始化指定名字模块的业务处理类
     *
     * @param moduleName
     * @throws Exception
     */
    private static void initModuleProc(String moduleName) throws Exception
    {
        m_procInstMap.remove(moduleName);
        Map<String, BaseProc> moduleProcInstMap = new ConcurrentHashMap<String, BaseProc>();
        m_procInstMap.put(moduleName, moduleProcInstMap);

        m_procMetaDataMap.remove(moduleName);
        Map<String, ProcMetaData> moduleProcMetaDataMap = new ConcurrentHashMap<String, ProcMetaData>();
        m_procMetaDataMap.put(moduleName, moduleProcMetaDataMap);

        String configData = ResourceUtil.getModuleDataResourceContent(moduleName,
                Config_File_Name);

        String defaultScope = XmlUtil.getXmlElement("proc-scope", configData, Scope_Singleton);

        List<String> procConfigList = XmlUtil.getAllXmlElements("proc", configData);
        for (String procConfig : procConfigList)
        {
            String procId = XmlUtil.getXmlElement("id", procConfig);
            if (MiscUtil.isEmpty(procId))
            {
                throw new Exception("id of proc can not be empty. moduleName:" + moduleName);
            }
            String procImpl = XmlUtil.getXmlElement("impl", procConfig);
            if (MiscUtil.isEmpty(procImpl))
            {
                throw new Exception("impl of proc can not be empty. id :" + procId + " moduleName:"
                        + moduleName);
            }
            String procScope = XmlUtil.getXmlElement("scope", procConfig, defaultScope);
            try
            {
                Class<?> procClazz = Class.forName(procImpl);
                Constructor<?> constructor = procClazz.getConstructor(String.class, String.class);
                BaseProc procObj = (BaseProc) constructor.newInstance(moduleName, procId);

                String params = XmlUtil.getXmlElement("params", procConfig);
                procObj.setParamData(params);
                procObj.init();

                if (procScope.equalsIgnoreCase(Scope_Singleton))
                {
                    moduleProcInstMap.put(procId, procObj);

//                    if (procObj instanceof IStatistics)
//                    {
//                        StatisticsManager.register(procObj);
//                    }
                }
                else if (procScope.equalsIgnoreCase(Scope_Prototype))
                {
                    ProcMetaData procMetaDataObj = new ProcMetaData(constructor, moduleName,
                            procId, params);
                    moduleProcMetaDataMap.put(procId, procMetaDataObj);
                }
                else
                {
                    throw new Exception("illegal scope:" + procScope + " id :" + procId
                            + " moduleName:" + moduleName);
                }
            }
            catch (InvocationTargetException ite)
            {
                Throwable exp = ite.getTargetException();
                throw new Exception(ProcManager.class.getName()
                        + " initialize initModuleProc failed:" + exp);
            }
        }
    }

    /**
     * 获取业务处理类实例
     *
     * @param data
     * @return
     * @throws BServerException
     */
    public static BaseProc getProc(RequestData data) throws BServerException
    {
        return getProc(data.getModuleName(), data.getProcId());
    }

    /**
     * 获取业务处理类实例
     *
     * @param moduleName
     *            业务处理模块名称
     * @param procId
     *            业务处理类Id
     * @return
     * @throws BServerException
     */
    public static BaseProc getProc(String moduleName, String procId) throws BServerException
    {
        Map<String, BaseProc> moduleProcInstMap = m_procInstMap.get(moduleName);
        Map<String, ProcMetaData> moduleProcMetaDataMap = m_procMetaDataMap.get(moduleName);
        if ((null == moduleProcInstMap) || (null == moduleProcMetaDataMap))
        {
            throw new UndefinedModuleException(moduleName);
        }
        BaseProc procObj = moduleProcInstMap.get(procId);
        if (null != procObj)
        {
            return procObj;
        }
        ProcMetaData procMetaDataObj = moduleProcMetaDataMap.get(procId);
        if (null != procMetaDataObj)
        {
            return procMetaDataObj.newInstance();
        }
        throw new UndefinedProcException(procId, moduleName);

    }
}
class ProcMetaData
{
    private String moduleName;
    private String procId;
    private String params;
    private Constructor<?> constructor;

    public ProcMetaData(Constructor<?> constructor, String moduleName, String procId, String params)
    {
        super();
        this.moduleName = moduleName;
        this.procId = procId;
        this.params = params;
        this.constructor = constructor;
    }

    public BaseProc newInstance() throws BServerException
    {
        try
        {
            BaseProc procObj = (BaseProc) constructor.newInstance(moduleName, procId);
            procObj.setParamData(params);
            procObj.init();
            return procObj;
        }
        catch (Exception exp)
        {
            throw new BServerException(ErrorCodeConstants.BServerError, exp.getMessage());
        }
    }
}