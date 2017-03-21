package kafeihu.zk.base.logging;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public enum LogLevel {

    /**
     * 定义日志级别常量。该定义顺序不可改变
     */
    DEBUG(10), INFO(20), WARN(30), ERROR(40);

    private int value = 0;

    private LogLevel(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

}
