package kafeihu.zk.base.security.ssl;

import kafeihu.zk.base.security.KeyStoreConfig;
import kafeihu.zk.base.security.SecureRandomConfig;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class SSLContextConfig {
    /**
     * 安全套接字协议名称，如：TLS
     */
    private String protocol;
    /**
     * 提供者名称
     */
    private String provider;

    /**
     * 证书/密钥配置
     */
    private KeyManagerFactoryConfig keyManagerFactoryConfig = null;
    /**
     * 信任的证书配置
     */
    private KeyManagerFactoryConfig trustManagerFactoryConfig = null;
    /**
     * 安全随机数配置
     */
    private SecureRandomConfig secureRandomConfig = null;

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {

        this.protocol = protocol;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public KeyManagerFactoryConfig getKeyManagerFactoryConfig()
    {
        return keyManagerFactoryConfig;
    }

    public void setKeyManagerFactoryConfig(KeyManagerFactoryConfig keyManagerFactoryConfig)
    {
        this.keyManagerFactoryConfig = keyManagerFactoryConfig;
    }

    public KeyManagerFactoryConfig getTrustManagerFactoryConfig()
    {
        return trustManagerFactoryConfig;
    }

    public void setTrustManagerFactoryConfig(KeyManagerFactoryConfig trustManagerFactoryConfig)
    {
        this.trustManagerFactoryConfig = trustManagerFactoryConfig;
    }

    public SecureRandomConfig getSecureRandomConfig()
    {
        return secureRandomConfig;
    }

    public void setSecureRandomConfig(SecureRandomConfig secureRandomConfig)
    {
        this.secureRandomConfig = secureRandomConfig;
    }

    /**
     * 解析SSL上下文配置文件，构造SSL上下文配置类
     *
     * @param xmlConfig
     * @return
     * @throws Exception
     */
    public static SSLContextConfig parseSSLContextConfig(String xmlConfig) throws Exception
    {
        SSLContextConfig config = new SSLContextConfig();

        // SSL协议
        String protocol = XmlUtil.getXmlElement("protocol", xmlConfig);
        if (MiscUtil.isEmpty(protocol))
        {
            throw new Exception("protocol can not be empty");
        }
        config.setProtocol(protocol);
        // SSL提供者
        String provider = XmlUtil.getXmlElement("provider", xmlConfig);
        config.setProvider(provider);

        // KeyManagerFactory配置
        String xmlKeyManagerConfig = XmlUtil.getXmlElement("keyManagerFactory", xmlConfig);
        if (!MiscUtil.isEmpty(xmlKeyManagerConfig))
        {
            KeyManagerFactoryConfig keyManagerConfig = parseKeyManagerConfig(xmlKeyManagerConfig);
            config.setKeyManagerFactoryConfig(keyManagerConfig);
        }
        // TrustManagerFactory配置
        String xmlTrustManagerConfig = XmlUtil.getXmlElement("trustManagerFactory", xmlConfig);
        if (!MiscUtil.isEmpty(xmlTrustManagerConfig))
        {
            KeyManagerFactoryConfig trustManagerConfig = parseKeyManagerConfig(xmlTrustManagerConfig);
            config.setTrustManagerFactoryConfig(trustManagerConfig);
        }
        // SecureRandom配置
        String xmlSecureRandomConfig = XmlUtil.getXmlElement("secureRandom", xmlConfig);
        if (!MiscUtil.isEmpty(xmlSecureRandomConfig))
        {
            SecureRandomConfig secureRandomConfig = parseSecureRandomConfig(xmlSecureRandomConfig);
            config.setSecureRandomConfig(secureRandomConfig);
        }
        return config;
    }

    private static KeyManagerFactoryConfig parseKeyManagerConfig(String xmlConfig) throws Exception
    {
        KeyManagerFactoryConfig config = new KeyManagerFactoryConfig();
        // 算法
        String algorithm = XmlUtil.getXmlElement("algorithm", xmlConfig);
        if (MiscUtil.isEmpty(algorithm))
        {
            throw new Exception("algorithm can not be empty");
        }
        config.setAlgorithm(algorithm);
        String provider = XmlUtil.getXmlElement("provider", xmlConfig);
        config.setProvider(provider);

        String xmlKeyStoreConfig = XmlUtil.getXmlElement("keyStore", xmlConfig);
        KeyStoreConfig keyStroeConfig = parseKeyStoreConfig(xmlKeyStoreConfig);
        config.setKeyStoreConfig(keyStroeConfig);
        return config;
    }

    private static KeyStoreConfig parseKeyStoreConfig(String xmlConfig) throws Exception
    {
        KeyStoreConfig config = new KeyStoreConfig();
        String password = XmlUtil.getOriginalXmlElement("password", xmlConfig, "");
        config.setPassword(password);
        String type = XmlUtil.getXmlElement("type", xmlConfig);
        if (MiscUtil.isEmpty(type))
        {
            throw new Exception("keyStore/type can not be empty");
        }
        config.setType(type);
        String location = XmlUtil.getXmlElement("location", xmlConfig);
        if (MiscUtil.isEmpty(location))
        {
            throw new Exception("keyStore/location can not be empty");
        }
        config.setLocation(location);
        String provider = XmlUtil.getXmlElement("provider", xmlConfig);
        config.setProvider(provider);
        return config;
    }

    private static SecureRandomConfig parseSecureRandomConfig(String xmlConfig) throws Exception
    {
        SecureRandomConfig config = new SecureRandomConfig();
        // 算法
        String algorithm = XmlUtil.getXmlElement("algorithm", xmlConfig);
        if (MiscUtil.isEmpty(algorithm))
        {
            throw new Exception("algorithm can not be empty");
        }
        config.setAlgorithm(algorithm);
        String provider = XmlUtil.getXmlElement("provider", xmlConfig);
        config.setProvider(provider);
        return config;
    }
}
