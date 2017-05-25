package kafeihu.zk.server.config;

import kafeihu.zk.base.util.XmlUtil;

import java.util.Properties;

/**
 *
 * 全局配置信息类
 * Created by zhangkuo on 2016/11/24.
 */
public class GlobalConfig {
    /**
     * 内部编码。默认为操作系统编码
     */
    public static String encoding = System.getProperty("file.encoding");

    /**
     * 耗时操作警告门限值：对于比较耗时的操作，进行报警
     */
    public static long timeConsumingWarningMills = 1500;
    private static Properties m_params;

    public static void init(String globalConfig) throws Exception
    {
        encoding = XmlUtil.getXmlElement("encoding", globalConfig, System
                .getProperty("file.encoding"));
        "ENCODING".getBytes(encoding);

        timeConsumingWarningMills = Long.parseLong(XmlUtil.getXmlElement("timeConsumingWarningMills", globalConfig, "1500"));


        m_params = XmlUtil.parseProperties(globalConfig);
    }

    /**
     * 获取全局配置数据。如果没有指定key的配置项，则返回空字符串
     * @param key
     * @return
     */
    public static String getGlobal(String key)
    {
        return m_params.getProperty(key,"");
    }

    /**
     * 获取全局配置数据。如果没有指定key的配置项，则返回指定的默认值
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getGlobal(String key, String defaultValue)
    {
        return m_params.getProperty(key, defaultValue);
    }

}
