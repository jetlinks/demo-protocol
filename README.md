# 演示自定义消息编解码协议

用于将自定义的MQTT消息格式编解码为平台的消息格式


# 使用

使用命名`mvn package` 打包后, 通过jetlinks管理界面`设备管理-协议管理`中上传jar包.
类名填写: `org.jetlinks.demo.protocol.DemoProtocolSupportProvider`


# TCP相关

报文生成和解码在test包下的src/test/java/org/jetlinks/demo/protocol/tcp/DemoTcpMessageTest.java类内

参考该类生成属性上报报文和属性回复报文及事件上报报文



