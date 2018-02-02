package kafeihu.zk.server.zkchat.core;

/**
 * Created by zhangkuo on 2017/6/18.
 */
public interface ZKChatErrorCode {


    /**
     * 系统处理类错误：09XX <br>
     * 数据库处理类错误：0901 - 0920<br>
     */
    /**
     * 数据库处理发生异常
     */
    String DBProc_Exception = "0901";
}
