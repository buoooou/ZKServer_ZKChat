package kafeihu.zk.bserver.netty;

import io.netty.channel.ChannelOption;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.base.util.reflect.ConstructParam;
import kafeihu.zk.base.util.reflect.InstanceBuilder;
import kafeihu.zk.bserver.netty.handler.factory.ChannelHandlerFactory;
import kafeihu.zk.bserver.netty.handler.factory.ChannelInitializerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServerConfig {
    /**
     * 服务器端口
     */
    private int port;
    /**
     * 当前接受的线程数
     */
    private int acceptorThreadNum = 1;
    /**
     * 工作线程数
     */
    private int workerThreadNum = 0;

    private ChannelHandlerFactory acceptorChannelHandlerFactory;
    private ChannelHandlerFactory workerChannelHandlerFactory;
    /**
     * acceptor Socket配置
     */
    private Map<String, Object> acceptorConnOptions = new ConcurrentHashMap<String, Object>();
    /**
     * worker Socket 配置
     */
    private Map<String, Object> workerConnOptions = new ConcurrentHashMap<String, Object>();

    public ChannelHandlerFactory getAcceptorChannelHandlerFactory()
    {
        return acceptorChannelHandlerFactory;
    }

    public void setAcceptorChannelHandlerFactory(ChannelHandlerFactory acceptorChannelHandlerFactory)
    {
        this.acceptorChannelHandlerFactory = acceptorChannelHandlerFactory;
    }

    public ChannelHandlerFactory getWorkerChannelHandlerFactory()
    {
        return workerChannelHandlerFactory;
    }

    public void setWorkerChannelHandlerFactory(ChannelHandlerFactory workerChannelHandlerFactory)
    {
        this.workerChannelHandlerFactory = workerChannelHandlerFactory;
    }

    public Map<String, Object> getAcceptorConnOption()
    {
        Map<String, Object> options = new ConcurrentHashMap<String, Object>();
        options.putAll(acceptorConnOptions);
        return options;
    }

    public Map<String, Object> getWorkerConnOption()
    {
        Map<String, Object> options = new ConcurrentHashMap<String, Object>();
        options.putAll(workerConnOptions);
        return options;
    }

    public void setAcceptorConnOption(String name, Object value)
    {
        if (ChannelOption.exists(name))
        {
            acceptorConnOptions.put(name, value);
        }
    }

    public void setAcceptorConnOption(Map<String, Object> options)
    {
        Set<String> names = options.keySet();
        for (String name : names)
        {
            setAcceptorConnOption(name, options.get(name));
        }
    }

    public void setWorkerConnOption(String name, Object value)
    {
        if (ChannelOption.exists(name))
        {
            // ChannelOption<Integer> option = ChannelOption.valueOf(name);
            workerConnOptions.put(name, value);
        }
    }

    public void setWorkerConnOption(Map<String, Object> options)
    {
        Set<String> names = options.keySet();
        for (String name : names)
        {
            setWorkerConnOption(name, options.get(name));
        }
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public int getAcceptorThreadNum()
    {
        return acceptorThreadNum;
    }

    public void setAcceptorThreadNum(int acceptorThreadNum)
    {
        this.acceptorThreadNum = acceptorThreadNum;
    }

    public int getWorkerThreadNum()
    {
        return workerThreadNum;
    }

    public void setWorkerThreadNum(int workerThreadNum)
    {
        this.workerThreadNum = workerThreadNum;
    }

    /**
     * 解析 XML配置文件 生成 NettyServerConfig
     *
     * @param xmlConfig
     * @return
     * @throws Exception
     */
    public static NettyServerConfig parseXmlConfig(String xmlConfig) throws Exception
    {
        NettyServerConfig nsc = new NettyServerConfig();

        // 设置服务器端口
        nsc.setPort(Integer.parseInt(XmlUtil.getXmlElement("serverPort", xmlConfig)));

        // ����Acceptor����
        String acceptorCfg = XmlUtil.getXmlElement("acceptor", xmlConfig);
        // Acceptor�̳߳ش�С��Ĭ��Ϊ1
        nsc.setAcceptorThreadNum(MiscUtil.parseInt(XmlUtil.getXmlElement("threadNum", acceptorCfg), 1));
        // ������������
        nsc.setAcceptorConnOption(parseConnOption(XmlUtil.getXmlElement("connOption", acceptorCfg)));

        // ��������������
        nsc.setAcceptorChannelHandlerFactory(buildHandlerFactory(XmlUtil.getXmlElement("handlerFactory", acceptorCfg)));

        // ����Worker����
        String workerCfg = XmlUtil.getXmlElement("worker", xmlConfig);
        // Worker�̳߳ش�С��Ĭ��Ϊ0��ʵ�ʴ�СΪCPU����*2
        nsc.setWorkerThreadNum(MiscUtil.parseInt(XmlUtil.getXmlElement("threadNum", workerCfg), 0));
        // ������������
        nsc.setWorkerConnOption(parseConnOption(XmlUtil.getXmlElement("connOption", workerCfg)));

        // ������ʼ������������
        ChannelHandlerFactory handlerInitFactory = buildHandlerFactory(XmlUtil.getXmlElement("handlerInitializerFactory", workerCfg));

        List<String> handlerFactoryCfgList = XmlUtil.getAllXmlElements("handlerFactory", workerCfg);
        for (String handlerFactoryCfg : handlerFactoryCfgList)
        {
            ((ChannelInitializerFactory) handlerInitFactory).addChannelHandlerFactory(buildHandlerFactory(handlerFactoryCfg));
        }
        nsc.setWorkerChannelHandlerFactory(handlerInitFactory);

        return nsc;
    }

    /**
     * �������Ӳ�������
     *
     * @param connOptionCfg
     * @return
     */
    private static Map<String, Object> parseConnOption(String connOptionCfg)
    {
        Map<String, Object> optionMap = new ConcurrentHashMap<String, Object>();
        List<String> optionNameList = XmlUtil.getAllXmlElementName(connOptionCfg);
        for (String optionName : optionNameList)
        {
            String value = XmlUtil.getXmlElement(optionName, connOptionCfg);
            if (MiscUtil.isEmpty(value))
            {
                continue;
            }
            if (value.equalsIgnoreCase("true"))
            {
                optionMap.put(optionName, true);

            }
            else if (value.equalsIgnoreCase("false"))
            {
                optionMap.put(optionName, false);
            }
            else
            {
                optionMap.put(optionName, Integer.parseInt(value));
            }
        }
        return optionMap;
    }

    private static ChannelHandlerFactory buildHandlerFactory(String handlerFactoryCfg) throws Exception
    {
        String handlerFactoryImpl = XmlUtil.getXmlElement("impl", handlerFactoryCfg);
        Properties handlerFactoryParam = XmlUtil.parseProperties(XmlUtil.getXmlElement("param", handlerFactoryCfg));

//        Instance<ChannelHandlerFactory> instBuilder = new Instance<ChannelHandlerFactory>();
        ConstructParam<?>[] params = new ConstructParam<?>[1];
        params[0] = new ConstructParam<Properties>(handlerFactoryParam, Properties.class);
//        return instBuilder.newInstance(handlerFactoryImpl, ChannelHandlerFactory.class, params);

        return  (ChannelHandlerFactory)new InstanceBuilder.Builder<ChannelHandlerFactory>(handlerFactoryImpl, ChannelHandlerFactory.class).setConstructParam(params).build();


    }
}
