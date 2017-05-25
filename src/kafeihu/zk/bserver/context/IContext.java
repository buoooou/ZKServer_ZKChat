package kafeihu.zk.bserver.context;

/**
 *
 * 应用上下文接口。<br>
 * 上下文实例用于加载、解析、保持系统或模块的扩展配置文件、运行环境等信息<br>
 * 扩展配置文件存放在相关应用或模块配置路径的ext目录下
 * Created by zhangkuo on 2016/11/24.
 */
public interface IContext {

    /**
     * 加载上下文配置数据。在系统启动时由框架自动调用该方法
     *
     * @param path
     *            配置数据文件所在路径
     * @throws Exception
     */
    void loadContextData(String path) throws Exception;

}
