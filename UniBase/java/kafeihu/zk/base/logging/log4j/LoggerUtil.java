package kafeihu.zk.base.logging.log4j;

import org.apache.log4j.Logger;

/**
 * Created by zhangkuo on 2017/8/23.
 */
public class LoggerUtil {

    private static Logger logger;

    public LoggerUtil(Logger logger) {
        this.logger=logger;
    }

    public static void info(String log){
        logger.info(log);
    };

    public static void warn(String log){
        logger.warn(log);
    };


    public static void error(String log){
        logger.error(log);
    };

    public static void debug(String log){
        logger.debug(log);
    };
}
