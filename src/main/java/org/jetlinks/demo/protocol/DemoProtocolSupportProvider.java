package org.jetlinks.demo.protocol;

import org.jetlinks.core.ProtocolSupport;
import org.jetlinks.core.Value;
import org.jetlinks.core.defaults.CompositeProtocolSupport;
import org.jetlinks.core.device.AuthenticationResponse;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.device.MqttAuthenticationRequest;
import org.jetlinks.core.message.codec.DefaultTransport;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.core.spi.ProtocolSupportProvider;
import org.jetlinks.core.spi.ServiceContext;
import org.jetlinks.demo.protocol.coap.CoAPDeviceMessageCodec;
import org.jetlinks.demo.protocol.http.HttpDeviceMessageCodec;
import org.jetlinks.demo.protocol.mqtt.MqttDeviceMessageCodec;
import org.jetlinks.demo.protocol.tcp.DemoTcpMessageCodec;
import org.jetlinks.demo.protocol.websocket.WebsocketDeviceMessageCodec;
import org.jetlinks.supports.official.JetLinksDeviceMetadataCodec;
import reactor.core.publisher.Mono;

public class DemoProtocolSupportProvider implements ProtocolSupportProvider {

    private static final DefaultConfigMetadata mqttConfig = new DefaultConfigMetadata(
        "MQTT认证配置"
        , "")
        .add("username", "username", "MQTT用户名", new StringType())
        .add("password", "password", "MQTT密码", new PasswordType());


    private static final DefaultConfigMetadata tcpConfig = new DefaultConfigMetadata(
        "TCP认证配置"
        , "")
        .add("tcp_auth_key", "key", "TCP认证KEY", new StringType());

    @Override
    public Mono<? extends ProtocolSupport> create(ServiceContext context) {
        CompositeProtocolSupport support = new CompositeProtocolSupport();
        support.setId("demo-v1");
        support.setName("演示协议v1");
        support.setDescription("演示协议");
        support.setMetadataCodec(new JetLinksDeviceMetadataCodec());

        context.getService(DeviceRegistry.class)
            .ifPresent(deviceRegistry -> {
                //TCP消息编解码器
                DemoTcpMessageCodec codec = new DemoTcpMessageCodec(deviceRegistry);
                support.addMessageCodecSupport(DefaultTransport.TCP, () -> Mono.just(codec));
                support.addMessageCodecSupport(DefaultTransport.TCP_TLS, () -> Mono.just(codec));

            });
        support.addConfigMetadata(DefaultTransport.TCP, tcpConfig);
        support.addConfigMetadata(DefaultTransport.TCP_TLS, tcpConfig);


        {
            //MQTT消息编解码器
            MqttDeviceMessageCodec codec = new MqttDeviceMessageCodec();
            support.addMessageCodecSupport(DefaultTransport.MQTT, () -> Mono.just(codec));
        }

        {
            //HTTP
            HttpDeviceMessageCodec codec = new HttpDeviceMessageCodec();
            support.addMessageCodecSupport(DefaultTransport.HTTP, () -> Mono.just(codec));
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
        support.addAuthenticator(DefaultTransport.MQTT, (request, device) -> {
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
        });

        return Mono.just(support);
    }
}
