package kafeihu.zk.bserver.zkchat.config;

import kafeihu.zk.base.util.XmlUtil;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public class ChatConfig {
    // 数据库最大查询记录数
    private int DBQueryMaxRecordCount;



    // 二维码流水号前缀
    private String QRNoPrefix;

    // 银行标志
    private String cmbBankFlag;

    // 交易币种
    private String currency;

    public ChatConfig()
    {
    }



    public String getQRNoPrefix()
    {
        return QRNoPrefix;
    }

    public String getCmbBankFlag()
    {
        return cmbBankFlag;
    }

    public String getCurrency()
    {
        return currency;
    }

    public int getDBQueryMaxRecordCount()
    {
        return DBQueryMaxRecordCount;
    }

    /**
     * 加载XML格式的配置数据
     *
     * @param xmlConfig
     * @throws Exception
     */
    public void loadXmlConfig(String xmlConfig) throws Exception
    {
        DBQueryMaxRecordCount = Integer.valueOf(XmlUtil.getXmlElement("DBQueryMaxRecordCount", xmlConfig, "5000"));

        QRNoPrefix = XmlUtil.getXmlElement("QRNoPrefix", xmlConfig);

        cmbBankFlag = XmlUtil.getXmlElement("cmbBankFlag", xmlConfig);

        currency = XmlUtil.getXmlElement("currency", xmlConfig);
    }
}
