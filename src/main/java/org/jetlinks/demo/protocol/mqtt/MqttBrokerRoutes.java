package org.jetlinks.demo.protocol.mqtt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.route.MqttRoute;

import java.util.StringJoiner;
import java.util.function.Function;

@Getter
public enum MqttBrokerRoutes {

    //上报属性数据
    reportProperty("/properties/#",
        route -> route
            .upstream(true)
            .downstream(false)
            .group("属性上报")
            .description("上报物模型属性数据")
            .example("{\"properties\":{\"属性ID\":\"属性值\"}}")),
    //上报属性数据
    event("/event/#",
        route -> route
            .upstream(true)
            .downstream(false)
            .group("事件上报")
            .description("上报物模型事件数据")
            .example("{\"data\":{\"key\":\"value\"}}")),
    ;


    private final String pattern;

    private final MqttRoute route;


    public MqttRoute getRoute() {
        return route;
    }

    MqttBrokerRoutes(String topic,
                     Function<MqttRoute.Builder, MqttRoute.Builder> routeCustom) {
        this.pattern = topic;
        this.route = routeCustom
            .apply(MqttRoute
                .builder(topic)
                .qos(1)).build();
    }
}
