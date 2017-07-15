package kafeihu.zk.base.logging;

import kafeihu.zk.base.util.MiscUtil;

/**
 * Created by zhangkuo on 2016/11/22.
 */
public class ConsoleLog implements ILog
{

    public void closeLog()
    {
    }

    public void flushLog()
    {
    }

    public void writeLog(String tips, String text)
    {
        StringBuilder sb = new StringBuilder(MiscUtil.getTimestamp());
        sb.append("  ").append(tips).append("  ").append(text);
        System.out.println(sb.toString());
    }

    /**
     * 构造一个ConsoleLog实例
     * @return
     */
    public static ILog buildLog()
    {
        return new ConsoleLog();
    }
    /**
     * 根据xml格式的配置数据，构造日志实例
     * @param xmlConfig
     * @return
     * @throws Exception
     */
    public static ILog buildLog(String xmlConfig) throws Exception
    {
        return new ConsoleLog();
    }

}

