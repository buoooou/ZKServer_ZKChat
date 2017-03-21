package kafeihu.zk.base.logging;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public interface ILog {

    /**
     * 写日志
     * @param tips
     * @param text
     */
    void writeLog(String tips, String text);
    /**
     * 刷新并强制写出所有缓冲的日志数据
     */
    void flushLog();
    /**
     * 关闭日志对象
     */
    void closeLog();

}
