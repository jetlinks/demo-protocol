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
                                            byte[] bytes=DemoTcpMessage.of(MessageType.REPORT_TEMPERATURE, data).toBytes();
                                            socket.write(Buffer.buffer(bytes),res->{
                                                if(!res.succeeded()){
                                                    res.cause().printStackTrace();;
                                                    return;
                                                }
                                                log.debug("send message:\n{}\n{}",data, Hex.encodeHexString(bytes));
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
