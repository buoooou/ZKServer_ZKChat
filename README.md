# UniBServer_ZKChat
通用socket服务器
## author
zhangkuo
## 功能
### 架构
#### UniBase
    基础层：
    包含基础工具包，通讯，日志，数据库，对象池，后台定时器，加密
#### UniBServer
    服务层：
    包含总管理器，socket服务器，monitor监控，定时任务服务器
#### UniBServer_ZKChat
    应用层：
    业务模块，处理业务逻辑
### 服务模块：
	1.SocketServerManager（Socket模块）
	socket请求
	配置文件为：socketserver-config.xml
	1.1、基于Socket实现的Thread-per-connection模式的SocketServer服务器<br>
     * 适用场景：<br>
     * 1.Socket短连接 <br>
     * 2.低客户端同时在线数量<br>
     * 3.高客户端响应体验
    1.2、基于NIO实现的Thread-on-event模式的SocketServer服务器<br>
     * 适用场景：<br>
     * 1.Socket长连接 <br>
     * 2.高客户端同时在线连接<br>
     * 3.客户端响应体验要求不高
	2.MonitorManager（monitor监控模块）
	实时监控页面Monitor.htm
	5s刷新一次，监控各个接口的请求量，响应时间
	3.ScheduleManager（定时任务模块）
    系统后台定时任务
    配置文件：schedule-config.xml
### 日志文件配置log-config.xml
    可修改日志文件
### 对象池pool-config.xml

### 系统维护文件
    systemMaintenance.txt
    修改文件状态，可日志提示该系统出于维护状态。
