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

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_REQUEST_QUEUE)
    public void listen(Message msg) {

        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();

        request.getBeerOrderDto().getBeerOrderLines()
                .forEach(orderLine -> orderLine.setQuantityAllocated(orderLine.getOrderQuantity()));

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder()
                        .allocationError(false)
                        .pendingInventory(false)
                        .beerOrderDto(request.getBeerOrderDto())
                        .build());
    }
}
