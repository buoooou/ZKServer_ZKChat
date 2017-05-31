package kafeihu.zk.console;

/**
 * Created by zhangkuo on 2016/11/21.
 */
public class Bserver {

    public static void main(String[] args) throws Exception{

        System.out.println("Unibserver start...");
        UniBserverManager.start();
        System.out.println("Unibserver started!");

    }
}
