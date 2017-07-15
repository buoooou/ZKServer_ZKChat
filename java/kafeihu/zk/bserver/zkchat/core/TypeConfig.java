package kafeihu.zk.bserver.zkchat.core;

import kafeihu.zk.base.util.IoUtil;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public class TypeConfig {
    // 发起交易的渠道类型列表
    private Map<String, String> channelTypes = new ConcurrentHashMap<String, String>(10);

    // 证件类型
    private Map<String, String> IDTypes = new ConcurrentHashMap<String, String>(10);

    // 允许的账户类型列表，如本行一卡通，本行信用卡，他行储蓄卡，他行信用卡
    private Map<String, String> acctTypes = new ConcurrentHashMap<String, String>(4);

    // 允许的账户标志列表，如支付卡，查询卡
    private Map<String, String> acctFlags = new ConcurrentHashMap<String, String>(4);

    // 允许的二维码请求类型列表，如付款码，收款码
    private Map<String, String> QRTypes = new ConcurrentHashMap<String, String>(4);

    // 允许的交易状态类型列表
    private Map<String, String> payStatuses = new ConcurrentHashMap<String, String>(6);

    // 允许的设备类型列表
    private Map<String, String> devTypes = new ConcurrentHashMap<String, String>(4);

    public Map<String, String> getChannelTypes()
    {
        return channelTypes;
    }

    public Map<String, String> getIDTypes()
    {
        return IDTypes;
    }

    public Map<String, String> getAcctTypes()
    {
        return acctTypes;
    }

    public Map<String, String> getAcctFlags()
    {
        return acctFlags;
    }

    public Map<String, String> getQRTypes()
    {
        return QRTypes;
    }

    public Map<String, String> getPayStatuses()
    {
        return payStatuses;
    }

    public Map<String, String> getDevTypes()
    {
        return devTypes;
    }

    public void loadConfig(String path) throws Exception
    {
        String xmlCfg = IoUtil.readTextFileContent(path + "Type-config.xml");

        String typesCfg = XmlUtil.getXmlElement("type-config", xmlCfg);

        List<String> typeList = XmlUtil.getAllXmlElements("type", typesCfg);

        for (String typeContent : typeList)
        {
            String id = XmlUtil.getXmlElement("id", typeContent).trim();
            String values = XmlUtil.getXmlElement("values", typeContent);
            // String description = XmlUtil.getXmlElement("description",
            // typeContent);

            List<String> valueList = XmlUtil.getAllXmlElements("value", values);
            if (id.equals("ChnType"))
            {
                bindCommValToRealVal(valueList, channelTypes, id);
            }
            else if (id.equals("IDType"))
            {
                bindCommValToRealVal(valueList, IDTypes, id);
            }
            else if (id.equals("AcctType"))
            {
                bindCommValToRealVal(valueList, acctTypes, id);
            }
            else if (id.equals("AcctFlag"))
            {
                bindCommValToRealVal(valueList, acctFlags, id);
            }
            else if (id.equals("QRType"))
            {
                bindCommValToRealVal(valueList, QRTypes, id);
            }
            else if (id.equals("PayStatus"))
            {
                bindCommValToRealVal(valueList, payStatuses, id);
            }
            else if (id.equals("DevType"))
            {
                bindCommValToRealVal(valueList, devTypes, id);
            }
        }
        System.out.println();
    }

    /**
     * 将通信数值与实际数值进行绑定
     *
     * @param xmlList
     * @param map
     * @param id
     * @throws Exception
     */
    private void bindCommValToRealVal(List<String> xmlList, Map<String, String> map, String id) throws Exception
    {
        for (String xml : xmlList)
        {
            String commVal = XmlUtil.getXmlElement("commValue", xml);
            String realVal = XmlUtil.getXmlElement("realValue", xml);

            if (MiscUtil.isEmpty(commVal) || MiscUtil.isEmpty(realVal))
            {
                throw new Exception("commVal/realVal in " + id + " can not be empty");
            }

            if (map.containsKey(commVal))
            {
                throw new Exception("duplicate commVal " + commVal + " exists in " + id);
            }

            if (map.containsValue(realVal))
            {
                throw new Exception("duplicate realVal " + realVal + " exists in " + id);
            }

            map.put(commVal, realVal);
        }
    }
}
