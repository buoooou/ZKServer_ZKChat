package kafeihu.zk.base.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * IP匹配器：判断指定的IP是否符合一定的规则
 *
 * Created by zhangkuo on 2017/6/8.
 */
public class IPPattern {

    private String DELIM = ",";

    private List<IPObj> listEqual = new ArrayList<IPObj>();
    private List<IPObj> listPattern = new ArrayList<IPObj>();
    private List<IPScope> listScope = new ArrayList<IPScope>();
    private List<IPObj> listExclude = new ArrayList<IPObj>();

    /**
     * 构造函数。配置数据格式为：<br>
     * <equal>a.b.c.d,a1.b1.c1.d1</equal> <br>
     * <pattern>a.b.*.*,a1.b1.c1.*</pattern><br>
     * <scope>a.b.c.d-a1.b1.c1.d1</scope> <br>
     * <exclude>a1.b2.c2.d2</exclude>
     *
     * @param ipRuleConfig
     * @throws Exception
     */
    public IPPattern(String ipRuleConfig) throws Exception
    {
        this(ipRuleConfig, ",");
    }

    public IPPattern(String ipRuleConfig, String delim) throws Exception
    {
        super();
        DELIM = delim;
        parseConfig(ipRuleConfig);
    }

    private void parseConfig(String strConfig) throws Exception
    {
        String strEqual = XmlUtil.getXmlElement("equal", strConfig);
        parseEqual(strEqual);

        String strPattern = XmlUtil.getXmlElement("pattern", strConfig);
        parsePattern(strPattern);

        String strScope = XmlUtil.getXmlElement("scope", strConfig);
        parseScope(strScope);

        String strExclude = XmlUtil.getXmlElement("exclude", strConfig);
        parseExclude(strExclude);
    }


    private void parseEqual(String strContent) throws Exception
    {
        String[] arrayObj = strContent.split(DELIM);
        for (int i = 0; i < arrayObj.length; i++)
        {
            String strOneObj = arrayObj[i].trim();
            if (!MiscUtil.isEmpty(strOneObj))
            {
                listEqual.add(new IPObj(strOneObj));
            }
        }
    }

    private void parsePattern(String strPattern) throws Exception
    {
        String[] arrayObj = strPattern.split(DELIM);
        for (int i = 0; i < arrayObj.length; i++)
        {
            String strOnePattern = arrayObj[i].trim();
            if (!MiscUtil.isEmpty(strOnePattern))
            {
                listPattern.add(new IPObj(strOnePattern));
            }
        }
    }

    private void parseScope(String strScope) throws Exception
    {
        String[] arrayObj = strScope.split(DELIM);
        for (int i = 0; i < arrayObj.length; i++)
        {
            String strOneScope = arrayObj[i].trim();
            if (!MiscUtil.isEmpty(strOneScope))
            {
                IPScope scope = new IPScope(strOneScope);
                listScope.add(scope);
            }
        }
    }

    private void parseExclude(String strExclude) throws Exception
    {
        String[] arrayObj = strExclude.split(DELIM);
        for (int i = 0; i < arrayObj.length; i++)
        {
            String strOneObj = arrayObj[i].trim();
            if (!MiscUtil.isEmpty(strOneObj))
            {
                listExclude.add(new IPObj(strOneObj));
            }
        }
    }

    private boolean matchEqual(IPObj obj)
    {
        return listEqual.contains(obj);
    }

    private boolean matchExclude(IPObj obj)
    {
        return listExclude.contains(obj);
    }

    private boolean matchPattern(IPObj obj)
    {
        int size = listPattern.size();
        for (int i = 0; i < size; i++)
        {
            IPObj pattern = listPattern.get(i);
            if (pattern.compareTo(obj) == 0)
            {
                return true;
            }
        }

        return false;
    }

    private boolean matchScope(IPObj obj)
    {
        int size = listScope.size();
        for (int i = 0; i < size; i++)
        {
            IPScope scope = listScope.get(i);
            if (scope.isInScope(obj))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * 判断给定IP对象是否匹配
     *
     * @param ipaddr
     * @return
     */
    public boolean match(String ipaddr)
    {
        try
        {
            IPObj obj = new IPObj(ipaddr);
            return match(obj);
        }
        catch (Exception exp)
        {
        }
        return false;
    }

    /**
     * 判断给定IP对象是否匹配
     *
     * @param obj
     * @return
     */
    public boolean match(IPObj obj)
    {
        if (matchEqual(obj))
        {
            return true;
        }
        if (matchExclude(obj))
        {
            return false;
        }
        if (matchScope(obj))
        {
            return true;
        }
        if (matchPattern(obj))
        {
            return true;
        }
        return false;
    }
}
