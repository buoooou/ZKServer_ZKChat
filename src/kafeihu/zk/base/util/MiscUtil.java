package kafeihu.zk.base.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public class MiscUtil {

    /**
     * yyyyMMdd HH:mm:ss
     *
     * @return
     */
    public static String getTimestamp()
    {
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        return getTimestamp(new Date());
    }

    /**
     * yyyyMMdd HH:mm:ss
     *
     * @return
     */
    public static String getTimestamp(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     *
     * 判断字符串是否为空
     * @param strValue
     * @return
     */
    public static boolean isEmpty(String strValue)
    {
        if (strValue == null)
        {
            return true;
        }
        if (strValue.length() <= 0)
        {
            return true;
        }
        return false;
    }

    /**
     * yyyyMMdd
     *
     *
     * @return
     */
    public static String getDate()
    {
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return getDate(new Date());
    }

    /**
     * yyyyMMdd
     *
     * @param date
     * @return
     */
    public static String getDate(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(date);
    }

    /**
     *  读取文件内容
     *
     * @param textFileUrl
     *            文件地址
     * @return
     * @throws Exception
     */
    public static String readTextFileContent(String textFileUrl) throws Exception
    {
        FileInputStream ins = new FileInputStream(textFileUrl);
        return readTextFileContent(ins);
    }

    /**
     * 读取文件内容
     *
     * @param ins
     *      输入流
     * @return
     * @throws Exception
     */
    public static String readTextFileContent(InputStream ins) throws Exception
    {
        InputStreamReader isr = null;
        try
        {
            isr = new InputStreamReader(ins);

            int len = ins.available();
            byte bTemp[] = new byte[len];

            ins.read(bTemp);
            return new String(bTemp, isr.getEncoding());

        }
        finally
        {
            if (isr != null)
            {
                isr.close();
            }
            if (ins != null)
            {
                ins.close();
            }

        }
    }

    /**
     * 字符串转int
     * @param str 字符串
     * @param defaultValue 默认值
     * @return
     */
    public static int parseInt(String str, int defaultValue)
    {
        try
        {
            return Integer.parseInt(str);
        }
        catch (Exception e)
        {
        }
        return defaultValue;
    }
    /**
     * 字符串转long
     * @param str 字符串
     * @param defaultValue 默认值
     * @return
     */
    public static long parseLong(String str, long defaultValue)
    {
        try
        {
            return Long.parseLong(str);
        }
        catch (Exception e)
        {
        }
        return defaultValue;
    }
    public static Integer toInteger(String str)
    {
        return toInteger(str,0);
    }
    /**
     * 字符串转int
     * @param str 字符串
     * @param defaultValue 默认值
     * @return
     */
    public static Integer toInteger(String str, int defaultValue)
    {
        try
        {
            return Integer.valueOf(str);
        }
        catch (Exception exp)
        {

        }
        return defaultValue;
    }

    /**
     *
     * @param h
     * @return
     */
    public static byte[] htonl(int h)
    {
        byte n[] = new byte[4];
        n[0] = (byte) ((h >> 24) & 0xff);
        n[1] = (byte) ((h >> 16) & 0xff);
        n[2] = (byte) ((h >> 8) & 0xff);
        n[3] = (byte) (h & 0xff);

        return n;
    }
    public static int ntohl(byte[] n)
    {
        int h = ((int) n[0] << 24) | (((int) n[1] << 16) & 0xffffff) | (((int) n[2] << 8) & 0xffff)
                | ((int) n[3] & 0xff);

        return h;
    }


    public static String formatStr(String strIn, int nWidth, char cFillChar, char cJustifyMode)
    {
        int nLen = strIn.length();

        if (nLen > nWidth)
        {
            return strIn.substring(0, nWidth);
        }
        else if (nLen == nWidth)
        {
            return strIn;
        }

        if (cJustifyMode == 'L' || cJustifyMode == 'l')
        {
            return strIn + fillWithChar(cFillChar, nWidth - nLen);
        }
        else if (cJustifyMode == 'R' || cJustifyMode == 'r')
        {
            return fillWithChar(cFillChar, nWidth - nLen) + strIn;
        }
        else
        {
            return strIn;
        }
    }

    public static String fillWithChar(char cFillChar, int nWidth)
    {
        StringBuilder sbResult = new StringBuilder(nWidth);
        for (int i = 0; i < nWidth; i++)
        {
            sbResult.append(cFillChar);
        }

        return sbResult.toString();
    }

    public static long getDuringMillisFromNextDayTime(int m_dayHour, int m_minute, int m_second) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, m_dayHour);
        cal.set(Calendar.MINUTE, m_minute);
        cal.set(Calendar.SECOND, m_second);

        //
        Calendar calNow = Calendar.getInstance();
        //
        if (cal.compareTo(calNow) <= 0)
        {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return cal.getTimeInMillis() - calNow.getTimeInMillis();
    }

    /***
     *
     * @param monthDay
     * @param dayHour
     * @param minute
     * @param second
     * @return
     */
    public static long getDuringMillisFromNextMonthDayTime(int monthDay, int dayHour, int minute,
                                                           int second)
    {
        Calendar cal = Calendar.getInstance();
        if (monthDay <= 0)
        {
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, monthDay - 1);
        }
        else
        {
            cal.set(Calendar.DAY_OF_MONTH, monthDay);
        }
        cal.set(Calendar.HOUR_OF_DAY, dayHour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        //
        Calendar calNow = Calendar.getInstance();
        //
        if (cal.compareTo(calNow) <= 0)
        {
            cal.add(Calendar.MONTH, 1);
        }
        return cal.getTimeInMillis() - calNow.getTimeInMillis();

    }

    /***
     *
     * @param weekDay
     * @param dayHour
     * @param minute
     * @param second
     * @return
     */
    public static long getDuringMillisFromNextWeekDayTime(int weekDay, int dayHour, int minute,
                                                          int second)
    {
        //
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, weekDay);
        cal.set(Calendar.HOUR_OF_DAY, dayHour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        //
        Calendar calNow = Calendar.getInstance();
        //
        if (cal.compareTo(calNow) <= 0)
        {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return cal.getTimeInMillis() - calNow.getTimeInMillis();
    }
}
