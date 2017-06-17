package kafeihu.zk.database;

import java.util.concurrent.Callable;

/**
 * 数据库处理工作者类，用于异步执行数据库操作
 *
 * Created by zhangkuo on 2017/6/17.
 */
public abstract class DBProcWorker implements Callable<DBProcResult> {

}
