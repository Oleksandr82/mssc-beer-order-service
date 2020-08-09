package tech.nautilus.beer.order.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    public static final String VALIDATE_ORDER_REQUEST_QUEUE = "validate-order-request";
    public static final String VALIDATE_ORDER_RESPONSE_QUEUE = "validate-order-response";

    public static final String ALLOCATE_ORDER_REQUEST_QUEUE = "allocate-order-request";
    public static final String ALLOCATE_ORDER_RESPONSE_QUEUE = "allocate-order-response";

    public static final String ALLOCATION_FAILURE_QUEUE = "allocation-failure";

    public static final String DEALLOCATE_ORDER_EVENT_QUEUE = "deallocate-order-event";

    public static final String TYPE_PROP_NAME = "_type";

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(TYPE_PROP_NAME);
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
