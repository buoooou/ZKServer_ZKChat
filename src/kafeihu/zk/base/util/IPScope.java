package kafeihu.zk.base.util;

/**
 * IP 地址范围类。 IP 地址大小比较规则：依次从高位到地位比较4段地址大小
 *
 * Created by zhangkuo on 2017/6/9.
 */
public class IPScope {

    private IPObj Scope_Max;
    private IPObj Scope_Min;

    public IPScope(String strScope) throws Exception
    {
        String[] arrayScope = strScope.split("-");
        if (arrayScope.length != 2)
        {
            throw new Exception("Illegal scope config:" + strScope);
        }
        Scope_Min = new IPObj(arrayScope[0]);
        Scope_Max = new IPObj(arrayScope[1]);
        if (Scope_Min.compareTo(Scope_Max) > 0)
        {
            Scope_Min = new IPObj(arrayScope[1]);
            Scope_Max = new IPObj(arrayScope[0]);
        }
    }

    /**
     * 判断给定的IP地址是否在当前范围内
     *
     * @param ipaddr
     * @return
     */
    public boolean isInScope(String ipaddr)
    {
        IPObj ipObj;
        try
        {
            ipObj = new IPObj(ipaddr);
            return isInScope(ipObj);
        }
        catch (Exception e)
        {
            return false;
        }
    }
    /**
     * 判断给定的IP对象是否在当前范围内
     * @param ipObj
     * @return
     */
    public boolean isInScope(IPObj ipObj)
    {
        if (ipObj.compareTo(Scope_Max) > 0)
        {
            return false;
        }
        if (ipObj.compareTo(Scope_Min) < 0)
        {
            return false;
        }
        return true;
    }

}
