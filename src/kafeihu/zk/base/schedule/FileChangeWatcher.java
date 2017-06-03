package kafeihu.zk.base.schedule;

import java.io.File;

/**
 * 文件变化监控器任务：监控指定文件最近修改时间变化，如果发生变化，触发特定的事件方法
 *
 * Created by zhangkuo on 2017/6/3.
 */
public abstract class FileChangeWatcher extends Task{
    // 目标文件
    private File m_targetFile;
    // 目标文件上次访问时间
    private volatile long m_lastModified;

    public FileChangeWatcher(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initialize() throws Exception
    {
        m_targetFile = getTargetFile();
        m_lastModified = m_targetFile.lastModified();
    }

    /**
     * 获取要监控的目标文件对象
     *
     * @return
     */
    protected abstract File getTargetFile() throws Exception;

    /**
     * 目标文件变化事件处理方法
     *
     * @param targetFile
     */
    protected abstract void onFileChanged(File targetFile) throws Exception;

    @Override
    public void execute(TaskExecutionContext context) throws Exception
    {
        long lastModified = m_targetFile.lastModified();
        if (lastModified != m_lastModified)
        {
            // 上次访问时间发生变化，触发事件
            onFileChanged(m_targetFile);
            m_lastModified = lastModified;
        }
    }
}
