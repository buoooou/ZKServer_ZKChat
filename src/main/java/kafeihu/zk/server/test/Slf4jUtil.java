package kafeihu.zk.server.test;

import kafeihu.zk.server.manager.ResourceManager;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhangkuo on 2017/8/26.
 */
public final class Slf4jUtil {

    final Logger logger = LoggerFactory.getLogger("console");
    Integer t;
    Integer oldT;

    public void setTemperature(Integer temperature){

        oldT = t;
        t = temperature;

        logger.debug("Temperature set to {}. Old temperature was {}.", t, oldT);

        if (temperature.intValue() > 50) {
            logger.info("Temperature has risen above 50 degrees.");
        }
    }
    public static void main(String[] args) {
        String path = ResourceManager.getSysDataPath();
        PropertyConfigurator.configure(path + "log4j.properties");
        new Slf4jUtil().setTemperature(4);
    }

}
