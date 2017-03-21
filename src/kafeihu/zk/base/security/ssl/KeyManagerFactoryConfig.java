package kafeihu.zk.base.security.ssl;

import kafeihu.zk.base.security.KeyStoreConfig;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class KeyManagerFactoryConfig {
    /**
     * KeyManager所用算法，如：SunX509
     */
    private String algorithm;
    /**
     * KeyManager的Provider名字，可以为空
     */
    private String provider;
    /**
     * KeyStore配置
     */
    private KeyStoreConfig keyStoreConfig;

    public String getAlgorithm()
    {
        return algorithm;
    }

    public void setAlgorithm(String algorithm)
    {
        this.algorithm = algorithm;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public KeyStoreConfig getKeyStoreConfig()
    {
        return keyStoreConfig;
    }

    public void setKeyStoreConfig(KeyStoreConfig keyStoreConfig)
    {
        this.keyStoreConfig = keyStoreConfig;
    }

}
