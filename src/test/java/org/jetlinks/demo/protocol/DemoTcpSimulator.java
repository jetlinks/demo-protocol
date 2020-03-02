package org.jetlinks.demo.protocol;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.demo.protocol.tcp.DemoTcpMessage;
import org.jetlinks.demo.protocol.tcp.MessageType;
import org.jetlinks.demo.protocol.tcp.TcpStatus;
import org.jetlinks.demo.protocol.tcp.message.AuthRequest;
import org.jetlinks.demo.protocol.tcp.message.AuthResponse;
import org.jetlinks.demo.protocol.tcp.message.TemperatureReport;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class DemoTcpSimulator {

    /*
    //平台-网络组件-TCP服务-处理方式-脚本-javascript:
    //平台侧处理粘拆包的脚本

     var BytesUtils = org.jetlinks.core.utils.BytesUtils;
     parser.fixed(5) //1. 固定5字节为报文头,0字节为类型,1-4字节为消息长度(低字节位在前).
       .handler(function(buffer){
            var len = BytesUtils.highBytesToInt(buffer.getBytes(),1,4);//2. 获取消息长度.
            parser
               .fixed(len)//3. 设置下一个包要读取固定长度的数据.
               .result(buffer); //4. 设置当前解析的结果
        })
       .handler(function(buffer){
            parser.result(buffer) //5. 收到了新的包,则为消息体,设置到结果中,完成后将与步骤4的数据合并为完整的数据包.
                   .complete(); //6. 完成解析(消息将进入协议中进行解析(DemoTcpMessageCodec)),重置解析器,下一个数据包将从步骤1开始解析.
        });

        */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        long deviceId = 1001;
        String key = "admin";
        vertx.createNetClient()
                .connect(5111, "127.0.0.1", result -> {
                    if (result.succeeded()) {
                        NetSocket socket = result.result();
                        socket.handler(buffer -> {
                            // TODO: 2020/3/2 粘拆包处理
                            DemoTcpMessage tcpMessage = DemoTcpMessage.of(buffer.getBytes());
                            System.out.println(tcpMessage);
                            //认证通过后定时上报温度数据
                            if (tcpMessage.getType() == MessageType.AUTH_RES && ((AuthResponse) tcpMessage.getData()).getStatus() == TcpStatus.SUCCESS) {
                                Flux.interval(Duration.ofSeconds(2))
                                        .map(t -> TemperatureReport.of(deviceId, (float) ThreadLocalRandom.current().nextDouble(20D, 50D)))
                                        .doOnNext(data -> {
                                            byte[] bytes = DemoTcpMessage.of(MessageType.REPORT_TEMPERATURE, data).toBytes();
                                            socket.write(Buffer.buffer(bytes), res -> {
                                                if (!res.succeeded()) {
                                                    res.cause().printStackTrace();
                                                    return;
                                                }
                                                log.debug("send message:\n{}\n{}", data, Hex.encodeHexString(bytes));
                                            });
                                        })
                                        .subscribe();
                            }
                        }).write(Buffer.buffer(DemoTcpMessage.of(MessageType.AUTH_REQ, AuthRequest.of(1001, key)).toBytes()), res -> {
                            if (!res.succeeded()) {
                                res.cause().printStackTrace();
                            }
                        }).exceptionHandler(Throwable::printStackTrace);

                    } else {
                        result.cause().printStackTrace();
                        System.exit(0);
                    }
                });
    }


}
