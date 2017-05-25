package kafeihu.zk.base.security;

import kafeihu.zk.server.config.ThreadPoolConfig;
import kafeihu.zk.base.security.ssl.KeyManagerFactoryConfig;
import kafeihu.zk.base.security.ssl.SSLContextConfig;
import kafeihu.zk.base.socket.SocketWorkerPool;
import kafeihu.zk.base.socket.SocketWorkerRejectedExecutionHandler;
import kafeihu.zk.base.util.MiscUtil;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangkuo on 2016/11/25.
 */
public class SecurityUtil {

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

    /**
     * 根据SSL上下文配置，构造SSL上下文实例
     *
     * @param sslContextConfig
     * @return
     * @throws Exception
     */
    public static SSLContext createSSLContext(SSLContextConfig sslContextConfig) throws Exception
    {
        KeyManager[] keyManagers = null;
        TrustManager[] trustManagers = null;
        SecureRandom secureRandom = null;
        // 构造服务器证书/密钥存储对象
        KeyManagerFactoryConfig kmfc = sslContextConfig.getKeyManagerFactoryConfig();
        if (null != kmfc)
        {
            KeyStoreConfig serverKeyStoreCfg = kmfc.getKeyStoreConfig();
            KeyStore serverKeystore;
            if (MiscUtil.isEmpty(serverKeyStoreCfg.getProvider()))
            {
                serverKeystore = KeyStore.getInstance(serverKeyStoreCfg.getType());
            }
            else
            {
                serverKeystore = KeyStore.getInstance(serverKeyStoreCfg.getType(),
                        serverKeyStoreCfg.getProvider());
            }
            InputStream is=new FileInputStream(serverKeyStoreCfg.getLocation());
            serverKeystore.load(is,
                    serverKeyStoreCfg.getPassword().toCharArray());
            is.close();
            // 构造密钥管理器工厂
            KeyManagerFactory keyManagerFactory;
            if (MiscUtil.isEmpty(kmfc.getProvider()))
            {
                keyManagerFactory = KeyManagerFactory.getInstance(kmfc.getAlgorithm());
            }
            else
            {
                keyManagerFactory = KeyManagerFactory.getInstance(kmfc.getAlgorithm(), kmfc
                        .getProvider());
            }
            keyManagerFactory.init(serverKeystore, serverKeyStoreCfg.getPassword().toCharArray());
            keyManagers = keyManagerFactory.getKeyManagers();
        }
        // 构造服务器可信证书/密钥存储对象

        KeyManagerFactoryConfig tmfc = sslContextConfig.getTrustManagerFactoryConfig();
        if (null != tmfc)
        {
            KeyStoreConfig serverTrustStoreCfg = tmfc.getKeyStoreConfig();
            KeyStore serverTrustStore;
            if (MiscUtil.isEmpty(serverTrustStoreCfg.getProvider()))
            {
                serverTrustStore = KeyStore.getInstance(serverTrustStoreCfg.getType());
            }
            else
            {
                serverTrustStore = KeyStore.getInstance(serverTrustStoreCfg.getType(),
                        serverTrustStoreCfg.getProvider());
            }
            InputStream is = new FileInputStream(serverTrustStoreCfg.getLocation());
            serverTrustStore.load(is, serverTrustStoreCfg.getPassword().toCharArray());
            is.close();

            TrustManagerFactory trustManagerFactory;
            if (MiscUtil.isEmpty(tmfc.getProvider()))
            {
                trustManagerFactory = TrustManagerFactory.getInstance(tmfc.getAlgorithm());
            }
            else
            {
                trustManagerFactory = TrustManagerFactory.getInstance(tmfc.getAlgorithm(), tmfc
                        .getProvider());
            }
            trustManagerFactory.init(serverTrustStore);
            trustManagers = trustManagerFactory.getTrustManagers();
        }
        // 构造安全随机数
        SecureRandomConfig srandCfg = sslContextConfig.getSecureRandomConfig();
        if (null != srandCfg)
        {
            if (MiscUtil.isEmpty(srandCfg.getProvider()))
            {
                secureRandom = SecureRandom.getInstance(srandCfg.getAlgorithm());
            }
            else
            {
                secureRandom = SecureRandom.getInstance(srandCfg.getAlgorithm(), srandCfg
                        .getProvider());
            }
        }

        SSLContext sslContext;
        if (MiscUtil.isEmpty(sslContextConfig.getProvider()))
        {
            sslContext = SSLContext.getInstance(sslContextConfig.getProtocol());
        }
        else
        {
            sslContext = SSLContext.getInstance(sslContextConfig.getProtocol(), sslContextConfig
                    .getProvider());
        }
        sslContext.init(keyManagers, trustManagers, secureRandom);
        return sslContext;
    }
}
