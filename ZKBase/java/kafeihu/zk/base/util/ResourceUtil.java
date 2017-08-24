package kafeihu.zk.base.util;

import kafeihu.zk.base.util.MiscUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 *
 * 资源管理器：操作相关的配置文件，日志文件等
 *
 * Created by zhangkuo on 2016/11/21.
 */
public class ResourceUtil {

    private static final String LogDirName = "log";
    private static final String ExtDataDirName = "extdata";

    private static String SysDataRoot;
    static
    {
        SysDataRoot = System.getProperty("app.dataroot");
        if (null == SysDataRoot)
        {
            // 如果未指定app.dataroot属性，取user.dir属性
            SysDataRoot = System.getProperty("user.dir") + File.separator + "dataroot"
                    + File.separator;
        }
        else
        {
            if (!SysDataRoot.endsWith(File.separator))
            {
                SysDataRoot = SysDataRoot + File.separator;
            }
        }
    }

    /**
     * 获取系统资源文件根目录
     *
     * @return
     */
    public static String getSysDataRoot()
    {
        return SysDataRoot;
    }

    /**
     * 获取系统配置文件目录
     *
     * @return
     */
    public static String getSysDataPath()
    {
        return SysDataRoot;
    }

    /**
     * 获取系统扩展配置数据文件目录
     *
     * @return
     */
    public static String getSysExtDataPath()
    {
        return SysDataRoot + ExtDataDirName + File.separator;
    }

    /**
     * 获取系统日志文件目录
     *
     * @return
     */
    public static String getSysLogPath()
    {
        return SysDataRoot + LogDirName + File.separator;
    }

    /**
     * 获取模块配置文件目录
     *
     * @return
     */
    public static String getModuleDataPath(String moduleName)
    {
        return SysDataRoot + moduleName + File.separator;
    }

    /**
     * 获取模块扩展配置数据文件目录
     *
     * @param moduleName
     * @return
     */
    public static String getModuleExtDataPath(String moduleName)
    {
        return SysDataRoot + moduleName + File.separator + ExtDataDirName + File.separator;
    }

    /**
     * 获取模块日志文件目录
     *
     * @return
     */
    public static String getModuleLogPath(String moduleName)
    {
        return SysDataRoot + moduleName + File.separator + LogDirName + File.separator;
    }

    /**
     * 获取系统配置文件输出流。使用完后需关闭资源
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public static InputStream getSysDataResourceAsStream(String filename) throws Exception
    {
        try
        {
            return new FileInputStream(getSysDataPath() + filename);
        }
        catch (FileNotFoundException exp)
        {
            throw new Exception("no resource with name='" + getSysDataPath() + filename
                    + "' is found");
        }
    }

    /**
     * 获取模块配置文件输出流。使用完后需关闭资源
     *
     * @param modulename
     * @param filename
     * @return
     * @throws Exception
     */
    public static InputStream getModuleDataResourceAsStream(String modulename, String filename)
            throws Exception
    {
        try
        {
            return new FileInputStream(getModuleDataPath(modulename) + filename);
        }
        catch (FileNotFoundException exp)
        {
            throw new Exception("no resource with name='" + getModuleDataPath(modulename)
                    + filename + "' is found");
        }
    }

    /**
     * 获取系统配置文件内容
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public static String getSysDataResourceContent(String filename) throws Exception
    {
        InputStream is = null;
        try
        {
            is = getSysDataResourceAsStream(filename);
            String configData = MiscUtil.readTextFileContent(is);
            return configData;
        }
        finally
        {
            try
            {
                if (null != is)
                {
                    is.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * 获取模块配置文件内容
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public static String getModuleDataResourceContent(String modulename, String filename)
            throws Exception
    {
        InputStream is = null;
        try
        {
            is = getModuleDataResourceAsStream(modulename, filename);
            String configData = MiscUtil.readTextFileContent(is);
            return configData;
        }
        finally
        {
            try
            {
                if (null != is)
                {
                    is.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * 获取系统配置文件
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public static File getSysDataResourceAsFile(String filename) throws Exception
    {
        File resFile = new File(getSysDataPath() + filename);
        if (!resFile.exists())
        {
            throw new Exception("no resource with name='" + getSysDataPath() + filename
                    + "' is found");
        }
        return new File(getSysDataPath() + filename);

    }

    /**
     * 获取模块配置文件
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public static File getModuleDataResourceAsFile(String modulename, String filename)
            throws Exception
    {
        File resFile = new File(getModuleDataPath(modulename) + filename);
        if (!resFile.exists())
        {
            throw new Exception("no resource with name='" + getModuleDataPath(modulename)
                    + filename + "' is found");
        }
        return new File(getModuleDataPath(modulename) + filename);
    }

    /**
     * 判断指定名字的系统资源文件是否存在
     *
     * @param filename
     * @return
     */
    public static boolean isSysDataResourceExists(String filename)
    {
        File resFile = new File(getSysDataPath() + filename);
        return resFile.exists();
    }

    /**
     * 判断指定名字的模块资源文件是否存在
     *
     * @param filename
     * @return
     */
    public static boolean isModuleDataResourceExists(String modulename, String filename)
    {
        File resFile = new File(getModuleDataPath(modulename) + filename);
        return resFile.exists();
    }


}
