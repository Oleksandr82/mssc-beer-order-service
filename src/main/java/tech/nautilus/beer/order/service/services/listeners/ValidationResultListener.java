package tech.nautilus.beer.order.service.services.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.order.service.config.JmsConfig;
import tech.nautilus.beer.order.service.services.BeerOrderManager;
import tech.nautilus.brewery.model.events.ValidateOrderResult;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void validateOrderResult(ValidateOrderResult result) {
        final UUID beerOrderId = result.getOrderId();
        log.debug("Validation Result for Order Id: {}", beerOrderId);
        beerOrderManager.processValidationResult(beerOrderId, result.getIsValid());
    }
}
