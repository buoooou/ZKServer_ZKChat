package kafeihu.zk.bserver.core.exception.model;

/**
 * 错误定义
 * Created by zhangkuo on 2016/11/24.
 */
public interface ErrorCodeConstants {

    /**
     * BServer处理错误
     */
    String BServerError = "BSR9999";

    /**
     * 功能模块未定义
     */
    String ModuleUndefined = "BSR1001";
    /**
     * ProcId未定义
     */
    String ProcUndefined = "BSR1002";
    /**
     * 非法Socket客户端连接
     */
    String IllegalClient = "BSR1003";
    /**
     * Cast请求数据失败：非RequestData类型
     */
    String CastRequestDataError="BSR1011";
    /**
     * Cast返回数据失败：非ResponseData类型
     */
    String CastResponseDataError="BSR1012";
    /**
     * 资源池资源对象耗尽
     */
    String NoFreePoolableObject = "BSR2010";
    /**
     * 获取资源对象失败
     */
    String BorrowPoolableObjectFailed = "BSR2020";
    /**
     * 获取资源对象失败：抛出SocketException
     */
    String BorrowPoolableObjectFailed_SoE = "BSR2021";
    /**
     * 获取资源对象失败：抛出IOException
     */
    String BorrowPoolableObjectFailed_IoE = "BSR2022";

    /**
     * 返回资源对象失败
     */
    String ReturnPoolableObjectFailed = "BSR2030";

    /**
     * SocketServer线程池忙
     */
    String ExecutorBusy = "BSR3001";
    /**
     * SocketServer线程池状态异常
     */
    String ExecutorAbnormal = "BSR3002";
    /**
     * SocketServer接收客户端请求数据失败
     */
    String ReceiveSocketClientRequestFailed = "BSR3003";
    /**
     * SocketServer返回响应数据给客户端失败
     */
    String SendResponse2SocketClientFailed = "BSR3004";


}
