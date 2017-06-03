package kafeihu.zk.base.schedule.policy.impl;

import kafeihu.zk.base.schedule.RepeatMode;
import kafeihu.zk.base.schedule.policy.Policy;

import java.util.Date;

/**
 * Created by zhangkuo on 2017/6/3.
 */
public class CyclePolicy implements Policy {
    @Override
    public int getRepeatCount() {
        return 0;
    }

    @Override
    public RepeatMode getRepeatMode() {
        return null;
    }

    @Override
    public Date getEndDate() {
        return null;
    }
}
