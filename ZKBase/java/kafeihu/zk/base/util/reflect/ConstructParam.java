package kafeihu.zk.base.util.reflect;

public class ConstructParam<P>
{
    public P getParamValue()
    {
        return m_paramValue;
    }

    public Class<P> getParamClazz()
    {
        return m_paramClazz;
    }

    private P m_paramValue;
    private Class<P> m_paramClazz;

    public ConstructParam(P value,Class<P> clazz)
    {
        m_paramValue = value;
        m_paramClazz = clazz;
    }

}
