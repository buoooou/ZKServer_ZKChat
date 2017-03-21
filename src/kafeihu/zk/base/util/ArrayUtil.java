package kafeihu.zk.base.util;

/**
 *
 * 数组操作工具类
 * Created by zhangkuo on 2016/11/25.
 */
public class ArrayUtil {
    /**
     * 复制一个字节数组
     *
     * @param array
     * @return
     */
    public static byte[] cloneArray(byte[] array)
    {
        if (array == null)
        {
            return null;
        }
        return (byte[]) array.clone();
    }

    /**
     * 合并两个字节数组
     *
     * @param array1
     * @param array2
     * @return
     */
    public static byte[] joinArray(byte[] array1, byte[] array2)
    {
        if (array1 == null)
        {
            return cloneArray(array2);
        }
        else if (array2 == null)
        {
            return cloneArray(array1);
        }
        byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * 反转字节数组
     * @param array
     */
    public static void reverseArray(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

}
