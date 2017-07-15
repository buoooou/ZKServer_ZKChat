package kafeihu.zk.base.security;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class KeyStoreConfig {
    /**
     * 密码
     */
    private String password;
    /**
     * KeyStore所在位置
     */
    private String location;
    /**
     * KeyStore实例类型，如：JKS
     */
    private String type;
    /**
     * KeyStore的Provider名字，可以为空
     */
    private String provider = "";

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
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
