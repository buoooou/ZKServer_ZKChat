package kafeihu.zk.bserver.netty.handler.factory;

public interface ChannelInitializerFactory {
    void addChannelHandlerFactory(ChannelHandlerFactory handlerFactory) throws Exception;
}
