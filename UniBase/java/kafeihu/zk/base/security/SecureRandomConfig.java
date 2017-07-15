package kafeihu.zk.base.security;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class SecureRandomConfig {
    /**
     * 算法
     */
    private String algorithm;
    /**
     * Provider名字，可以为空
     */
    private String provider;

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
}
