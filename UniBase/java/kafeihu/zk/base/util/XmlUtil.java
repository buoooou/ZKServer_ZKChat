package kafeihu.zk.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public class XmlUtil {

    /**
     * 取Xml字符串中指定字段值。默认为空字符串。
     *
     * @param strElementName Xml字段名
     * @param strSource Xml源字符串
     * @return
     */
    public static String getXmlElement(String strElementName, String strSource)
    {
        return getXmlElement(strElementName, strSource, "");
    }

    /**
     *
     * 取Xml字符串中指定字段值，去掉空字符。
     *
     * @param strElementName Xml字段名
     * @param strSource Xml源字符串
     * @param strDefaultValue 默认值
     * @return
     */
    public static String getXmlElement(String strElementName, String strSource, String strDefaultValue)
    {
        String strHead = "<" + strElementName + ">";
        String strTail = "</" + strElementName + ">";

        int iHeadPos = strSource.indexOf(strHead);
        int iTailPos = strSource.indexOf(strTail);
        if (iHeadPos == -1 || iTailPos == -1)
        {
            return strDefaultValue;
        }

        int iHeadSize = strHead.length();
        String strElementValue = strSource.substring(iHeadPos + iHeadSize, iTailPos);
        return strElementValue.trim();
    }

    /**
     * 取Xml字符串中指定字段值。对结果不进行trim操作
     * @param strElementName
     * @param strSource
     * @param strDefaultValue
     * @return
     */
    public static String getOriginalXmlElement(String strElementName, String strSource, String strDefaultValue)
    {
        String strHead = "<" + strElementName + ">";
        String strTail = "</" + strElementName + ">";

        int iHeadPos = strSource.indexOf(strHead);
        int iTailPos = strSource.indexOf(strTail);
        if (iHeadPos == -1 || iTailPos == -1)
        {
            return strDefaultValue;
        }

        int iHeadSize = strHead.length();
        String strElementValue = strSource.substring(iHeadPos + iHeadSize, iTailPos);
        return strElementValue;
    }

    /**
     * 取Xml源字符串中指定字段的所有值
     *
     * @param strElementName Xml字段名
     * @param strSource Xml源字符串
     * @return
     */
    public static List<String> getAllXmlElements(String strElementName, String strSource)
    {
        List<String> listElem = new ArrayList<String>();
        String strHead = "<" + strElementName + ">";
        String strTail = "</" + strElementName + ">";
        int iHeadLen = strHead.length();
        int iTailLen = strTail.length();
        while (true)
        {
            int iHeadPos = strSource.indexOf(strHead);
            if (iHeadPos == -1)
            {
                break;
            }

            int iTailPos = strSource.indexOf(strTail);
            if (iTailPos == -1)
            {
                break;
            }
            String strElement = strSource.substring(iHeadPos + iHeadLen, iTailPos);
            listElem.add(strElement.trim());
            strSource = strSource.substring(iTailPos + iTailLen);
        }
        return listElem;
    }

    /**
     * 获取Xml字符串中所有的字段名
     *
     * @param strSource
     * @return
     */
    public static List<String> getAllXmlElementName(String strSource)
    {
        List<String> listElemName = new ArrayList<String>();
        if (null != strSource)
        {
            while (true)
            {
                int iStartPos = strSource.indexOf('<');
                if (iStartPos == -1)
                {
                    break;
                }
                int iEndPos = strSource.indexOf('>', iStartPos);
                if (iEndPos == -1)
                {
                    break;
                }

                if (strSource.charAt(iStartPos + 1) == '!')
                {// 注释
                    strSource = strSource.substring(iEndPos);
                    continue;
                }
                String strElemName = strSource.substring(iStartPos + 1, iEndPos);
                String strEndXmlTag = "</" + strElemName + ">";
                int iEndTagPos = strSource.indexOf(strEndXmlTag);
                if (iEndTagPos == -1)
                {
                    break;
                }
                listElemName.add(strElemName);
                strSource = strSource.substring(iEndTagPos + strEndXmlTag.length());
            }
        }
        return listElemName;
    }

    /**
     * 将Xml字符串转换为Properties。每个字段映射为Properties的一个属性
     *
     * @param strXmlContent
     * @return
     */
    public static Properties parseProperties(String strXmlContent)
    {
        Properties prop = new Properties();
        List<String> listElemName = getAllXmlElementName(strXmlContent);
        int iSize = listElemName.size();
        for (int i = 0; i < iSize; i++)
        {
            String strElemName = listElemName.get(i);
            String strElemValue = getXmlElement(strElemName, strXmlContent);
            prop.setProperty(strElemName, strElemValue);
        }
        return prop;
    }
}
