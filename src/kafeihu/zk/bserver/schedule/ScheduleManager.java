package kafeihu.zk.bserver.schedule;

import kafeihu.zk.base.schedule.Scheduler;
import kafeihu.zk.bserver.manager.base.ServiceManager;

/**
 *
 * 任务调度管理器：在系统启动时，按配置进行任务调度
 *
 * Created by zhangkuo on 2017/5/30.
 */
public class ScheduleManager extends ServiceManager {
    /**
     * 配置文件名
     */
    private final static String Config_File_Name = "schedule-config.xml";

    /**
     * 调度器
     */
    //private final static Scheduler m_scheduler = Scheduler.getDefaultScheduler();


}
