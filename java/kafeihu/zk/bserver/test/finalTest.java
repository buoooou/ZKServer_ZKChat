package kafeihu.zk.bserver.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *  变量的初始化次序优于任何方法，甚至在构造方法的前面。
 *  对于static变量也是一样，
 *  如果变量是原始类型，那么它得到一个标准的原始类型的初始值，
 *  如果是一个对象的引用，除非你创建了一个新的对象给这个引用，否则就是null。
 *  static变量在需要的时候才会初始化，并且在这个类的构造函数和所有其他普通变量之前调用，static之后就不再进行初始化了，
 *  static变量在类初始化时(注意不是实例)，就必须分配内存空间，
 *  static变量单独划分一块存储空间。
 *  java类首次装入时，会对静态成员变量或方法进行一次初始化，
 *  先初始化父类的静态代码-->初始化子类的静态代码-->
 *  (创建使历史，如果不创建实例，则后面的不执行)初始化父类的非静态代码-->初始化父类的构造
 *  -->初始化子类的非静态代码-->初始化子类的构造
 *  类只有在使用new调用创建的时候才会被java类装载器装入。
 *
 *
 *  final 反射可变
 *
 *  Integer 缓存
 *
 *  整数区间 -128 到 +127。最大值 127 可以通过 JVM 的启动参数 -XX:AutoBoxCacheMax=size 修改。
 *  这种缓存行为不仅适用于Integer对象。我们针对所有整数类型的类都有类似的缓存机制。
 *  有 ByteCache 用于缓存 Byte 对象
 *  有 ShortCache 用于缓存 Short 对象
 *  有 LongCache 用于缓存 Long 对象
 *  有 CharacterCache 用于缓存 Character 对象
 *  Byte，Short，Long 有固定范围: -128 到 127。对于 Character, 范围是 0 到 127。除了 Integer  可以通过参数改变范围外，其它的都不行。
 * Created by zhangkuo on 2017/9/2.
 */
public class finalTest {

    private static final Integer code = 1;

    private static String a="";
    public finalTest(String a){

        this.a=a;
    }
    static {
        System.out.println(code);
        System.out.println(a);

    }
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

        finalSwitch();

        //integerCache();
    }

    private static void integerCache(){

        Integer integer1 = new Integer(3);
        Integer integer2 = new Integer(3);
        System.out.println("integer1:"+integer1+" integer2:"+integer2);
        if (integer1 == integer2)
            System.out.println("integer1 == integer2");
        else
            System.out.println("integer1 != integer2");

        Integer integer3 = 300;
        Integer integer4 = 300;

        if (integer3 == integer4)
            System.out.println("integer3 == integer4");
        else
            System.out.println("integer3 != integer4");

        System.out.println("integer3:"+integer3+" integer4:"+integer4);

        Integer integer5 = 3;
        Integer integer6 = 3;
        System.out.println("integer5:"+integer5+" integer6:"+integer6);
        if (integer5 == integer6)
            System.out.println("integer5 == integer6");
        else
            System.out.println("integer5 != integer6");
    }

    private static void finalSwitch() throws NoSuchFieldException, IllegalAccessException{
        System.out.println(code);

        Field field= finalTest.class.getDeclaredField("code");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field,field.getModifiers()&~Modifier.FINAL);

        field.set(null,100);

        System.out.println(code);

    }
}
