package kafeihu.zk.base.util;

import java.io.*;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public class IoUtil {
    /**
     * 从指定输入流读数据，直到输入流无数据为止。完成后关闭输入流
     *
     * @param iStream
     * @return
     */
    public static byte[] readData(InputStream iStream) throws IOException
    {
        byte[] result = null;
        try
        {
            int size = iStream.available();
            if (size < 1024)
            {
                size = 1024;
            }
            byte[] data = new byte[size];
            while (true)
            {
                int nRet = iStream.read(data);
                if (nRet < 0)
                {// 达到EOF，返回-1
                    break;
                }
                byte[] readBytes = new byte[nRet];
                System.arraycopy(data, 0, readBytes, 0, nRet);

                result = ArrayUtil.joinArray(result, readBytes);
            }
            if (null == result)
            {
                return new byte[0];
            }
            return result;
        }
        finally
        {
            try
            {
                iStream.close();
            }
            catch (Exception exp)
            {
            }
        }
    }

    /**
     * 从指定输入流读数据到字节数组，完成后关闭输入流。
     *
     * @param iStream
     *            输入流
     * @param baBuffer
     *            接受数据存放的字节数组
     * @param nMaxReceiveByte
     *            最大接收字节数
     * @return 实际接收的字节数
     * @throws IOException
     */
    public static int readData(InputStream iStream, byte[] baBuffer,
                               int nMaxReceiveByte) throws IOException
    {
        return readData(iStream, baBuffer, 0, nMaxReceiveByte);
    }

    /**
     * 从指定输入流读数据到字节数组，完成后关闭输入流。从字节数组的指定位置开始存放接受数据。
     *
     * @param baBuffer
     *            接受数据存放的字节数组
     * @param off
     *            字节数组存放数据的开始位置
     * @param nMaxReceiveByte
     *            最大接收字节数
     * @return 实际接收的字节数
     * @throws IOException
     */
    public static int readData(InputStream iStream, byte[] baBuffer, int off,
                               int nMaxReceiveByte) throws IOException
    {
        try
        {
            int nTotalReaded = 0;
            while (nTotalReaded < nMaxReceiveByte)
            {
                int nThisTimeReaded = iStream.read(baBuffer,
                        nTotalReaded + off, nMaxReceiveByte - nTotalReaded);
                if (nThisTimeReaded < 0)
                {// 达到EOF，返回-1
                    if (0 == nTotalReaded)
                    {
                        return -1;
                    }
                    break;
                }
                nTotalReaded += nThisTimeReaded;
            }

            return nTotalReaded;
        }
        finally
        {
            try
            {
                iStream.close();
            }
            catch (Exception exp)
            {
            }
        }
    }

    /**
     * 读取文本文件内容
     *
     * @param textFilePath
     *            文本文件存放路径
     * @param textFileName
     *            文本文件名
     * @return
     * @throws Exception
     */
    public static String readTextFileContent(String textFilePath,
                                             String textFileName) throws Exception
    {
        String fullFileName = "";
        if (textFilePath.endsWith(File.separator))
        {
            fullFileName = textFilePath + textFileName;
        }
        else
        {
            fullFileName = textFilePath + File.separator + textFileName;
        }
        return readTextFileContent(fullFileName);
    }

    /**
     * 读取文本文件内容
     *
     * @param textFileUrl
     *            文本文件绝对路径
     * @return
     * @throws Exception
     */
    public static String readTextFileContent(String textFileUrl)
            throws Exception
    {
        FileInputStream ins = new FileInputStream(textFileUrl);
        return readTextFileContent(ins);
    }

    /**
     * 读文本文件内容
     *
     * @param ins
     *            指向文本文件的输入流
     * @return
     * @throws Exception
     */
    public static String readTextFileContent(InputStream ins) throws Exception
    {
        InputStreamReader isr = null;
        try
        {
            isr = new InputStreamReader(ins);

            byte[] bContent = readData(ins);
            return new String(bContent, isr.getEncoding());

        }
        finally
        {
            try
            {
                if (isr != null)
                {
                    isr.close();
                }
            }
            catch (Exception exp)
            {
            }
        }
    }
}
