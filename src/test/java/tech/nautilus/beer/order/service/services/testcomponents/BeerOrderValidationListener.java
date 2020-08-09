package tech.nautilus.beer.order.service.services.testcomponents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.order.service.config.JmsConfig;
import tech.nautilus.brewery.model.events.ValidateOrderRequest;
import tech.nautilus.brewery.model.events.ValidateOrderResult;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    public static final String FAIL_VALIDATION = "fail-validation";
    public static final String SKIP_VALIDATION = "skip-validation";

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_REQUEST_QUEUE)
    public void listen(Message msg) {

        ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

        if (Optional.ofNullable(request.getBeerOrder().getCustomerRef())
                .map(ref -> !ref.equals(SKIP_VALIDATION)).orElse(true)) {

            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResult.builder()
                            .isValid(Optional.ofNullable(request.getBeerOrder().getCustomerRef())
                                    .map(ref -> !ref.equals(FAIL_VALIDATION)).orElse(true))
                            .orderId(request.getBeerOrder().getId())
                            .build());
        }
    }
}
