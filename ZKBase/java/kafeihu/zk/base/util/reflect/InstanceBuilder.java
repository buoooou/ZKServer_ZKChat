package kafeihu.zk.base.util.reflect;

import kafeihu.zk.base.util.MiscUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class InstanceBuilder<T> {


    public static class Builder<T>{

        private String className;
        private Class<T> clazz;
        private ConstructParam<?>[] params;

        public Builder(String clazzName, Class<T> clazz){
            this.className=clazzName;
            this.clazz=clazz;
        }
        public Builder setConstructParam(ConstructParam<?>... params){
            this.params=params;
            return this;
        }

        public T build() throws Exception{
            return new InstanceBuilder<T>().newInstance(this);
        }

    }
    private InstanceBuilder(){}

    private T newInstance(Builder<T> builder)throws Exception{

        String clazzName = builder.className;
        Class<T> clazz= builder.clazz;
        ConstructParam<?>[] params = builder.params;

        try
        {
            if (MiscUtil.isEmpty(clazzName))
            {
                throw new Exception("class name can not be empty for " + clazz.getName());

            }
            Class<?> cls = Class.forName(clazzName);
            if (!clazz.isAssignableFrom(cls))
            {
                throw new Exception(clazzName + " is not type of " + clazz.getName());
            }
            int len = params.length;
            Class<?>[] clazzArray = new Class<?>[len];
            Object[] valueArray = new Object[len];
            for (int i=0; i<len; i++)
            {
                clazzArray[i] = params[i].getParamClazz();
                valueArray[i] = params[i].getParamValue();
            }

            Constructor<?> constructor = cls.getConstructor(clazzArray);
            return clazz.cast(constructor.newInstance(valueArray));
        }
        catch (InvocationTargetException exp)
        {
            throw new Exception(exp.getTargetException());
        }
    }

    public static void main(String[] args) throws Exception
    {
        String clazzName = "java.lang.String";
        ConstructParam<?>[] params = new ConstructParam<?>[1];
        ConstructParam<String> param0 = new ConstructParam<String>("StringExample", String.class);
        params[0] = param0;
        String s=(String) new InstanceBuilder.Builder<String>(clazzName,String.class).setConstructParam(params).build();
        System.out.println("Inst:"+s);

//        InstanceBuilder<String> ibStr = new InstanceBuilder<String>();
//        String clazzName = "java.lang.String";
//        ConstructParam<?>[] params = new ConstructParam<?>[1];
//        ConstructParam<String> param0 = new ConstructParam<String>("StringExample", String.class);
//        params[0] = param0;
//
//        String inst = ibStr.newInstance(clazzName, String.class);
//        System.out.println("Inst:"+inst);
//
//
//        inst = ibStr.newInstance(clazzName, String.class, params);
//        System.out.println("Inst:"+inst);
//
//
//        params = new ConstructParam<?>[2];
//        param0 = new ConstructParam<String>("127.0.0.1", String.class);
//        params[0] = param0;
//        ConstructParam<Integer> param1 = new ConstructParam<Integer>(8010, int.class);
//        params[1] = param1;
//        InstanceBuilder<Socket> ibSock = new InstanceBuilder<Socket>();
//        Socket instSock = ibSock.newInstance("java.net.Socket", Socket.class, params);
//        System.out.println("SocketInst:"+instSock.toString());
    }
}
