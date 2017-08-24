package kafeihu.zk.base.util;

import java.util.Arrays;

/**
 * Created by zhangkuo on 2017/6/8.
 */
public class IPObj implements Comparable<IPObj>{

    private int [] ipAddr=new int[]{0,0,0,0};

    public IPObj(String strIP) throws Exception{
        init(strIP);
    }

    private void init(String strIP) throws Exception{

        String [] array=strIP.split("\\.");
        int len =array.length;
        if(len!=4){
            throw new Exception("illegal ip :"+strIP);
        }
        ipAddr[0]=parseIPart(array[0]);
        ipAddr[1]=parseIPart(array[1]);
        ipAddr[2]=parseIPart(array[2]);
        ipAddr[3]=parseIPart(array[3]);
    }

    private int parseIPart(String ippart)
    {
        try
        {
            return Integer.valueOf(ippart);
        }
        catch (Exception e)
        {
        }
        return -1;
    }

    public int getIPart(int index)
    {
        if ((index >= 4) || (index < 0))
        {
            return -1;
        }
        return ipAddr[index];
    }

    public int compareTo(IPObj ipObj) {
        for (int i = 0; i < 4; i++)
        {
            if ((ipAddr[i] >= 0) && (ipObj.getIPart(i) >= 0))
            {
                if (ipAddr[i] > ipObj.getIPart(i))
                {
                    return 1;
                }
                else if (ipAddr[i] < ipObj.getIPart(i))
                {
                    return -1;
                }
            }
        }
        return 0;
    }
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(ipAddr);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IPObj other = (IPObj) obj;
        if (!Arrays.equals(ipAddr, other.ipAddr))
            return false;
        return true;
    }
}
