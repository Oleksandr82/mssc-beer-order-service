package tech.nautilus.beer.order.service.services.testcomponents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.order.service.config.JmsConfig;
import tech.nautilus.brewery.model.events.AllocateOrderRequest;
import tech.nautilus.brewery.model.events.AllocateOrderResult;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    public static final String FAIL_ALLOCATION = "fail-allocation";
    public static final String PENDING_ALLOCATION = "pending-allocation";

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_REQUEST_QUEUE)
    public void listen(Message msg) {

        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();

        request.getBeerOrderDto().getBeerOrderLines()
                .forEach(orderLine -> orderLine.setQuantityAllocated(orderLine.getOrderQuantity()));

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder()
                        .allocationError(Optional.ofNullable(request.getBeerOrderDto().getCustomerRef())
                                .map(ref -> ref.equals(FAIL_ALLOCATION)).orElse(false))
                        .pendingInventory(Optional.ofNullable(request.getBeerOrderDto().getCustomerRef())
                                .map(ref -> ref.equals(PENDING_ALLOCATION)).orElse(false))
                        .beerOrderDto(request.getBeerOrderDto())
                        .build());
    }
}
