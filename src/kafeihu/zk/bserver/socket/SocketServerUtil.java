package kafeihu.zk.bserver.socket;

import kafeihu.zk.base.server.socket.SocketWorkerPool;
import kafeihu.zk.bserver.config.ThreadPoolConfig;
import kafeihu.zk.base.server.socket.ISocketServer;
import kafeihu.zk.base.server.socket.ISocketExceptionHandler;
import kafeihu.zk.base.server.socket.ISocketRequestHandler;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;
import kafeihu.zk.base.server.socket.SocketWorkerRejectedExecutionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * SocketServer包相关工具类
 * Created by zhangkuo on 2016/11/24.
 */
public class SocketServerUtil {


    /**
     * 根据Socket工作线程池配置对象构造线程池实例
     *
     * @return
     * @throws Exception
     */
    public static SocketWorkerPool createSocketWorkerPool(ThreadPoolConfig poolConfig) throws Exception
    {
        BlockingQueue<Runnable> blockQueue = null;
        if (poolConfig.getWorkQueueSize() > 0)
        {
            blockQueue = new ArrayBlockingQueue<Runnable>(poolConfig.getWorkQueueSize());
        }
        else
        {
            blockQueue = new SynchronousQueue<Runnable>();
        }

        SocketWorkerRejectedExecutionHandler handler;
        String rejectedExecutionHandler = poolConfig.getRejectedExecutionHandler();
        if (MiscUtil.isEmpty(rejectedExecutionHandler))
        {
            throw new Exception("rejectedExecutionHandler of SocketWorkerPool can not be empty");
        }
        else
        {
            Object temp = Class.forName(poolConfig.getRejectedExecutionHandler()).newInstance();
            if (temp instanceof SocketWorkerRejectedExecutionHandler)
            {
                handler = (SocketWorkerRejectedExecutionHandler) temp;

            }
            else
            {
                throw new Exception("rejectedExecutionHandler must be instance of "
                        + SocketWorkerRejectedExecutionHandler.class.getName());
            }
        }

        SocketWorkerPool pool = new SocketWorkerPool(poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(),
                poolConfig.getKeepAliveTimeSeconds(), TimeUnit.SECONDS, blockQueue, handler);
        return pool;
    }

//    /**
//     * 根据NIOSocket工作线程池配置对象构造线程池实例
//     *
//     * @return
//     * @throws Exception
//     */
//    public static NioSocketWorkerPool createNioSocketWorkerPool(ThreadPoolConfig poolConfig) throws Exception
//    {
//        BlockingQueue<Runnable> blockQueue = null;
//        if (poolConfig.getWorkQueueSize() > 0)
//        {
//            blockQueue = new ArrayBlockingQueue<Runnable>(poolConfig.getWorkQueueSize());
//        }
//        else
//        {
//            blockQueue = new SynchronousQueue<Runnable>();
//        }
//
//        NioSocketWorkerRejectedExecutionHandler handler;
//        String rejectedExecutionHandler = poolConfig.getRejectedExecutionHandler();
//        if (MiscUtil.isEmpty(rejectedExecutionHandler))
//        {
//            throw new Exception("rejectedExecutionHandler of SocketWorkerPool can not be empty");
//        }
//        else
//        {
//            Object temp = Class.forName(poolConfig.getRejectedExecutionHandler()).newInstance();
//            if (temp instanceof NioSocketWorkerRejectedExecutionHandler)
//            {
//                handler = (NioSocketWorkerRejectedExecutionHandler) temp;
//
//            }
//            else
//            {
//                throw new Exception("rejectedExecutionHandler must be instance of "
//                        + NioSocketWorkerRejectedExecutionHandler.class.getName());
//            }
//        }
//
//        NioSocketWorkerPool pool = new NioSocketWorkerPool(poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(),
//                poolConfig.getKeepAliveTimeSeconds(), TimeUnit.SECONDS, blockQueue, handler);
//        return pool;
//    }

    /**
     * 根据SocketServer配置，构造SocketServer实例。<br>
     * 要求：具体的SocketServer实现类必须有静态方法buildSocketServer
     *
     * @param xmlSocketServerConfig
     * @return
     * @throws Exception
     */
    public static ISocketServer createSocketServer(String xmlSocketServerConfig) throws Exception
    {
        String serverImpl = XmlUtil.getXmlElement("serverImpl", xmlSocketServerConfig);
        if (MiscUtil.isEmpty(serverImpl))
        {
            throw new Exception("serverImpl of socketServer can not be empty");
        }
        try
        {
            Class<?> serverClazz = Class.forName(serverImpl);
            Method buildMethod = serverClazz.getMethod("buildSocketServer", String.class);
            ISocketServer socketServer = (ISocketServer) buildMethod.invoke(serverClazz, xmlSocketServerConfig);
            return socketServer;
        }
        catch (InvocationTargetException exp)
        {
            throw new Exception("buildSocketServer failed:" + exp.getTargetException());
        }
        catch (Exception exp)
        {
            throw new Exception("buildSocketServer failed:" + exp);
        }
    }

    /**
     * 根据Socket请求处理器配置，构造请求处理器实例。<br>
     * 要求：具体的请求处理器类必须有静态方法buildSocketRequestHandler
     *
     * @param xmlHandlerConfig
     * @return
     */
    public static ISocketRequestHandler createSocketRequestHandler(String xmlHandlerConfig) throws Exception
    {
        String handlerImpl = XmlUtil.getXmlElement("handlerImpl", xmlHandlerConfig);
        if (MiscUtil.isEmpty(handlerImpl))
        {
            throw new Exception("handlerImpl of SocketRequestHandler can not be empty");
        }
        try
        {
            Class<?> handlerClazz = Class.forName(handlerImpl);
            Method buildMethod = handlerClazz.getMethod("buildSocketRequestHandler", String.class);
            ISocketRequestHandler requestHandler = (ISocketRequestHandler) buildMethod.invoke(handlerClazz,
                    xmlHandlerConfig);
            return requestHandler;
        }
        catch (InvocationTargetException exp)
        {
            throw new Exception("buildSocketRequestHandler failed:" + exp.getTargetException());
        }
        catch (Exception exp)
        {
            throw new Exception("buildSocketRequestHandler failed:" + exp);
        }
    }

    /**
     * 根据Socket请求异常处理器配置，构造处理器实例。<br>
     * 要求：具体的请求处理器类必须有静态方法buildSocketExceptionHandlerr
     *
     * @param xmlHandlerConfig
     * @return
     */
    public static ISocketExceptionHandler createSocketExceptionHandler(String xmlHandlerConfig) throws Exception
    {
        String handlerImpl = XmlUtil.getXmlElement("handlerImpl", xmlHandlerConfig);
        if (MiscUtil.isEmpty(handlerImpl))
        {
            throw new Exception("handlerImpl of SocketExceptionHandler can not be empty");
        }
        try
        {
            Class<?> handlerClazz = Class.forName(handlerImpl);
            Method buildMethod = handlerClazz.getMethod("buildSocketExceptionHandler", String.class);
            ISocketExceptionHandler requestHandler = (ISocketExceptionHandler) buildMethod.invoke(handlerClazz,
                    xmlHandlerConfig);
            return requestHandler;
        }
        catch (InvocationTargetException exp)
        {
            throw new Exception("buildSocketExceptionHandler failed:" + exp.getTargetException());
        }
        catch (Exception exp)
        {
            throw new Exception("buildSocketExceptionHandler failed:" + exp);
        }
    }

//    /**
//     * 根据Socket请求处理器配置，构造请求处理器实例。<br>
//     * 要求：具体的请求处理器类必须有静态方法buildSocketRequestHandler
//     *
//     * @param xmlHandlerConfig
//     * @return
//     */
//    public static INioSocketRequestHandlerFactory createNioSocketRequestHandlerFactory(String xmlHandlerConfig)
//            throws Exception
//    {
//        String handlerImpl = XmlUtil.getXmlElement("factoryImpl", xmlHandlerConfig);
//        if (MiscUtil.isEmpty(handlerImpl))
//        {
//            throw new Exception("factoryImpl of NioSocketRequestHandlerFactory can not be empty");
//        }
//        try
//        {
//            Class<?> handlerClazz = Class.forName(handlerImpl);
//            Method buildMethod = handlerClazz.getMethod("buildNioSocketRequestHandlerFactory", String.class);
//            INioSocketRequestHandlerFactory factory = (INioSocketRequestHandlerFactory) buildMethod.invoke(
//                    handlerClazz, xmlHandlerConfig);
//            return factory;
//        }
//        catch (InvocationTargetException exp)
//        {
//            throw new Exception("buildSocketRequestHandler failed:" + exp.getTargetException());
//        }
//        catch (Exception exp)
//        {
//            throw new Exception("buildSocketRequestHandler failed:" + exp);
//        }
//    }


}
