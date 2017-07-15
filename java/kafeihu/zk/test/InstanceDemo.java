package kafeihu.zk.test;

/**
 * Created by zhangkuo on 2017/5/29.
 */
public class InstanceDemo {
    public static void main(String[] args) {

    }
    static InstanceDemo m_instance=null;
    private InstanceDemo(){}
    public static InstanceDemo getInstacne(){

        if (m_instance==null){

            createInstance();

        }
        return  m_instance;

        //方法2
       // return InstanceUtil.m_instance;


    }
    private static synchronized  InstanceDemo createInstance(){

        if(m_instance==null){

            m_instance=new InstanceDemo();
        }
        return m_instance;
    }

    //完美内部类,方法2
    public static class InstanceUtil{
        private final static InstanceDemo m_instance= new InstanceDemo();

    }

}
