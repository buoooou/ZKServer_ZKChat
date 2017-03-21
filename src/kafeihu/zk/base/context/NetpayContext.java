package kafeihu.zk.base.context;

import kafeihu.zk.base.config.NetpayConfig;
import kafeihu.zk.base.config.TypeConfig;
import kafeihu.zk.base.util.IoUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public class NetpayContext extends ModuleContext{
    /**
     * netpay模块共用配置
     */
    public static NetpayConfig netpayConfig;

    public static TypeConfig typeConfig;
    /**
     * 错误码
     */
    public static Properties ErrorCodeMessage = new Properties();

    public NetpayContext(String mModuleName)
    {
        super(mModuleName);
    }

    public void loadContextData(String path) throws Exception
    {
        // 加载netpay模块共用配置
        loadNetpayConfig(path);
        // 加载netpay模块错误码
        loadErrorCode(path);

        loadTypeConfig(path);
    }

    /**
     * 加载netpay模块共用配置
     * @param path
     * @throws Exception
     */
    private void loadNetpayConfig(String path) throws Exception
    {
        InputStream is = null;

        try
        {
            String xmlConfig = IoUtil.readTextFileContent(path + "Netpay-config.xml");
            netpayConfig = new NetpayConfig();
            netpayConfig.loadXmlConfig(xmlConfig);
        }
        catch (Exception e)
        {
            throw new Exception(NetpayContext.class.getName() + ".loadNetpayConfig() failed : " + e.getMessage());
        }
        finally
        {
            try
            {
                if (null != is)
                {
                    is.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }
    /**
     * 加载netpay模块错误码
     * @param path
     */
    private void loadErrorCode(String path) throws Exception
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(path + "errorcode.xml");
            ErrorCodeMessage.loadFromXML(is);
        }
        catch (Exception exp)
        {
            throw new Exception(NetpayContext.class.getName()+ ".loadErrorCode() failed : " + exp.getMessage());
        }
        finally
        {
            try
            {
                if (null != is)
                {
                    is.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    private void loadTypeConfig(String path) throws Exception
    {
        try
        {
            typeConfig = new TypeConfig();
            typeConfig.loadConfig(path);
        }
        catch (Exception exp)
        {
            throw new Exception(NetpayContext.class.getName()+ ".loadTypeConfig() failed : " + exp.getMessage());
        }

    }

}
