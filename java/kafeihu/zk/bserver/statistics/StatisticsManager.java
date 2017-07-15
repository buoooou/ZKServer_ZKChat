package kafeihu.zk.bserver.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 统计管理器。管理所有需要进行状态统计的对象
 *
 * Created by zhangkuo on 2017/6/10.
 */
public class StatisticsManager {

    /***
     * 所有需要进行状态统计的对象
     */
    private static List<IStatistics> m_statObj =new ArrayList();

    /**
     * 统计对象取消注册
     *
     * @param stateObj
     */
    public static synchronized  void unRegister(IStatistics stateObj){
        m_statObj.remove(stateObj);
    }

    /**
     * 注销所有统计对象
     */
    public static synchronized void unRegisterAll(){
        m_statObj.clear();
    }

    /**
     * 注册统计对象
     *
     * @param statObj
     */
    public static synchronized void register(IStatistics statObj)
    {
        //先删除对象。防止重复注册
        m_statObj.remove(statObj);
        //增加对象
        m_statObj.add(statObj);
    }

    /**
     * 获取系统所有状态统计数据
     *
     * @return
     */
    public static synchronized List<Object> getStaticticsData()
    {
        List<Object> statData = new ArrayList<Object>();
        for (IStatistics statObj : m_statObj)
        {
            statData.add(statObj.getStatistics());
        }
        return statData;
    }
}
