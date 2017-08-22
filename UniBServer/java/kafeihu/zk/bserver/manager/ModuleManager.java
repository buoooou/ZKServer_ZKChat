package kafeihu.zk.bserver.manager;

import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.ResourceUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public final class ModuleManager {

    /**
     * 配置文件名
     */
    private final static String Config_File_Name = "module-config.xml";
    /**
     * 活跃的模块名称列表
     */
    private static List<String> m_activeModuleNameList = new ArrayList<String>();

    static
    {
        try
        {
            System.out.print("Initializing ModuleManager...... ");
            initialize();
            System.out.println("OK!");
        }
        catch (Exception exp)
        {
            throw new ExceptionInInitializerError(ModuleManager.class.getName()
                    + ".initialize().  " + exp);
        }
    }

    /**
     * 解析活跃模块名称列表并验证配置数据是否正确
     *
     * @return
     * @throws Exception
     */
    private static void initialize() throws Exception
    {
        String configData = ResourceUtil.getSysDataResourceContent(Config_File_Name);

        List<String> moduleConfigList = XmlUtil.getAllXmlElements("module", configData);
        for (String moduleConfig : moduleConfigList)
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
            if (m_activeModuleNameList.contains(moduleName))
            {
                throw new Exception("duplicate module defined with same name : " + moduleName);
            }
            String contextImpl = XmlUtil.getXmlElement("contextImpl", moduleConfig);
            if (MiscUtil.isEmpty(contextImpl))
            {
                throw new Exception("contextImpl of module can not be empty. moduleName:"
                        + moduleName);
            }
            m_activeModuleNameList.add(moduleName);
        }

        if(m_activeModuleNameList.size() <=0)
        {
            throw new Exception("illegal module configuration : no active module");
        }
    }

    /**
     * 获取所有活跃的模块名称
     *
     * @return
     */
    public static List<String> getModuleName()
    {
        return m_activeModuleNameList;
    }
}
