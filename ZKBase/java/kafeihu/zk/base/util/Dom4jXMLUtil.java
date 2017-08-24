package kafeihu.zk.base.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public class Dom4jXMLUtil {

    public static void main(String[] args) throws DocumentException {
        String str="<csdn> <java>Java班</java></csdn>";
        System.out.println(getXmlElement(str,"java"));


        String text = "<csdn> <java>Java班</java></csdn>";
        Document document = DocumentHelper.parseText(text);
        Element root = document.getRootElement();
        String value=root.element("java").getTextTrim();
        System.out.println(value);
    }

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
    public static String getXmlElement(String strElementName, String strSource, String strDefaultValue) {
        try {
            Document document = DocumentHelper.parseText(strSource);
            Element root = document.getRootElement();
            String value=root.element(strElementName).getTextTrim();
            if(value.isEmpty()){
                return strDefaultValue;
            }
            return value;
        } catch (DocumentException e) {
            return strDefaultValue;
        }
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
        Document document = null;
        try {
            document = DocumentHelper.parseText(strSource);
        } catch (DocumentException e) {
            return strDefaultValue;
        }
        Element root = document.getRootElement();
        String value=root.element(strElementName).getText();
        if(value.isEmpty()){
            return strDefaultValue;
        }
        return value;
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
        Document document = null;
        try {
            document = DocumentHelper.parseText(strSource);
        } catch (DocumentException e) {

        }
        Element root = document.getRootElement();
        List<Element> list= root.elements(strElementName);

        for (Element element : list){
            listElem.add(element.getTextTrim());
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
        List<String> listElem = new ArrayList<String>();
        Document document = null;
        try {
            document = DocumentHelper.parseText(strSource);
        } catch (DocumentException e) {

        }
        Element root = document.getRootElement();
        for(Iterator it = root.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            listElem.add(element.getName());
        }
        return listElem;
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
