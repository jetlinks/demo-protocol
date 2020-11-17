package org.jetlinks.demo.protocol;

import org.jetlinks.core.ProtocolSupport;
import org.jetlinks.core.Value;
import org.jetlinks.core.defaults.Authenticator;
import org.jetlinks.core.defaults.CompositeProtocolSupport;
import org.jetlinks.core.device.*;
import org.jetlinks.core.device.manager.DeviceBindManager;
import org.jetlinks.core.message.codec.DefaultTransport;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.DeviceConfigScope;
import org.jetlinks.core.metadata.types.IntType;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.core.server.session.DeviceSessionManager;
import org.jetlinks.core.spi.ProtocolSupportProvider;
import org.jetlinks.core.spi.ServiceContext;
import org.jetlinks.demo.protocol.coap.CoAPDeviceMessageCodec;
import org.jetlinks.demo.protocol.http.HttpClientDeviceMessageCodec;
import org.jetlinks.demo.protocol.http.HttpDeviceMessageCodec;
import org.jetlinks.demo.protocol.mqtt.MqttDeviceMessageCodec;
import org.jetlinks.demo.protocol.tcp.DemoTcpMessageCodec;
import org.jetlinks.demo.protocol.udp.DemoUdpMessageCodec;
import org.jetlinks.demo.protocol.websocket.WebsocketDeviceMessageCodec;
import org.jetlinks.supports.official.JetLinksDeviceMetadataCodec;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public class DemoProtocolSupportProvider implements ProtocolSupportProvider {


    @Override
    public void dispose() {
        //协议卸载时执行
    }

    private static final DefaultConfigMetadata mqttConfig = new DefaultConfigMetadata(
        "MQTT认证配置"
        , "")
        .add("username", "username", "MQTT用户名", StringType.GLOBAL)
        .add("password", "password", "MQTT密码", PasswordType.GLOBAL);


    private static final DefaultConfigMetadata tcpConfig = new DefaultConfigMetadata(
        "TCP认证配置"
        , "")
        .add("tcp_auth_key", "key", "TCP认证KEY", StringType.GLOBAL);

    private static final DefaultConfigMetadata udpConfig = new DefaultConfigMetadata(
        "UDP认证配置"
        , "")
        .add("udp_auth_key", "key", "UDP认证KEY", StringType.GLOBAL);

    private static final DefaultConfigMetadata tcpClientConfig = new DefaultConfigMetadata(
        "远程服务配置"
        , "")
        .add("host", "host", "host", StringType.GLOBAL, DeviceConfigScope.product)//只需要产品配置
        .add("port", "port", "host", IntType.GLOBAL, DeviceConfigScope.product);//只需要产品配置

    @Override
    public Mono<? extends ProtocolSupport> create(ServiceContext context) {
        CompositeProtocolSupport support = new CompositeProtocolSupport();
        support.setId("demo-v1");
        support.setName("演示协议v1");
        support.setDescription("演示协议");
        //固定为JetLinksDeviceMetadataCodec
        support.setMetadataCodec(new JetLinksDeviceMetadataCodec());

        //TCP 演示协议
        {
            DemoTcpMessageCodec codec = new DemoTcpMessageCodec();
            support.addMessageCodecSupport(DefaultTransport.TCP, () -> Mono.just(codec));
            support.addMessageCodecSupport(DefaultTransport.TCP_TLS, () -> Mono.just(codec));
            support.addConfigMetadata(DefaultTransport.TCP, tcpConfig);
            support.addConfigMetadata(DefaultTransport.TCP_TLS, tcpConfig);

        }

        {
            //UDP消息编解码器
            DemoUdpMessageCodec codec = new DemoUdpMessageCodec();
            support.addMessageCodecSupport(DefaultTransport.UDP, () -> Mono.just(codec));
            support.addMessageCodecSupport(DefaultTransport.UDP_DTLS, () -> Mono.just(codec));

            support.addConfigMetadata(DefaultTransport.UDP, udpConfig);
            support.addConfigMetadata(DefaultTransport.UDP_DTLS, udpConfig);
        }

        {
            //MQTT消息编解码器
            MqttDeviceMessageCodec codec = new MqttDeviceMessageCodec();
            support.addMessageCodecSupport(DefaultTransport.MQTT, () -> Mono.just(codec));
        }

        {
            //DeviceBindManager 支持绑定第三方平台信息
            context.getService(DeviceBindManager.class)
                   .ifPresent(bind->{
                       //HTTP
                       HttpDeviceMessageCodec codec = new HttpDeviceMessageCodec(bind);
                       support.addMessageCodecSupport(DefaultTransport.HTTP, () -> Mono.just(codec));
                   });
        }

        {
            //HTTP Client
            Mono.zip(
                Mono.justOrEmpty(context.getService(DeviceRegistry.class)),
                Mono.justOrEmpty(context.getService(DeviceSessionManager.class)),
                HttpClientDeviceMessageCodec::new
                )
                .subscribe(codec -> {

                    //注册到协议
                    //support.addMessageCodecSupport(DefaultTransport.HTTP, () -> Mono.just(codec));

                });
        }


        {
            //CoAP
            CoAPDeviceMessageCodec codec = new CoAPDeviceMessageCodec();
            support.addMessageCodecSupport(DefaultTransport.CoAP, () -> Mono.just(codec));
        }

        {
            //WebSocket
            WebsocketDeviceMessageCodec codec = new WebsocketDeviceMessageCodec();
            support.addMessageCodecSupport(DefaultTransport.WebSocket, () -> Mono.just(codec));
        }

        //MQTT需要的配置信息
        support.addConfigMetadata(DefaultTransport.MQTT, mqttConfig);
        //MQTT认证策略
        support.addAuthenticator(DefaultTransport.MQTT, new Authenticator() {
            @Override
            //使用clientId作为设备ID时的认证方式
            public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request, @Nonnull DeviceOperator device) {
                MqttAuthenticationRequest mqttRequest = ((MqttAuthenticationRequest) request);
                return device.getConfigs("username", "password")
                    .flatMap(values -> {
                        String username = values.getValue("username").map(Value::asString).orElse(null);
                        String password = values.getValue("password").map(Value::asString).orElse(null);
                        if (mqttRequest.getUsername().equals(username) && mqttRequest.getPassword().equals(password)) {
                            return Mono.just(AuthenticationResponse.success());
                        } else {
                            return Mono.just(AuthenticationResponse.error(400, "密码错误"));
                        }

                    });
            }

            @Override
            //在网关中配置使用指定的认证协议时的认证方式
            public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request, @Nonnull DeviceRegistry registry) {
                MqttAuthenticationRequest mqttRequest = ((MqttAuthenticationRequest) request);
                return registry
                    .getDevice(mqttRequest.getUsername()) //用户名作为设备ID
                    .flatMap(device -> device
                        .getSelfConfig("password").map(Value::asString) //密码
                        .flatMap(password -> {
                            if (password.equals(mqttRequest.getPassword())) {
                                //认证成功，需要返回设备ID
                                return Mono.just(AuthenticationResponse.success(mqttRequest.getUsername()));
                            } else {
                                return Mono.just(AuthenticationResponse.error(400, "密码错误"));
                            }
                        }));
            }
        });

//        //tcp client,通过tcp客户端连接其他服务处理设备消息
//        {
//            support.addConfigMetadata(DefaultTransport.TCP, tcpClientConfig);
//            return Mono
//                .zip(
//                    Mono.justOrEmpty(context.getService(DecodedClientMessageHandler.class)),
//                    Mono.justOrEmpty(context.getService(DeviceSessionManager.class)),
//                    Mono.justOrEmpty(context.getService(Vertx.class))
//                )
//                .map(tp3 -> new TcpClientMessageSupport(tp3.getT1(), tp3.getT2(), tp3.getT3()))
//                .doOnNext(tcp -> {
//                    //设置状态检查
//                    support.setDeviceStateChecker(tcp);
//                    support.doOnDispose(tcp); //协议失效时执行
//                })
//                .thenReturn(support);
//
//        }

        return Mono.just(support);
    }
}
