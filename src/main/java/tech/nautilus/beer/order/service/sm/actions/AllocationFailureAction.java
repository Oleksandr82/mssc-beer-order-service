package tech.nautilus.beer.order.service.sm.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.order.service.domain.BeerOrderEventEnum;
import tech.nautilus.beer.order.service.domain.BeerOrderStatusEnum;
import tech.nautilus.beer.order.service.services.BeerOrderManagerImpl;
import tech.nautilus.brewery.model.events.AllocationFailureEvent;

import java.util.UUID;

import static tech.nautilus.beer.order.service.config.JmsConfig.ALLOCATION_FAILURE_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);

        jmsTemplate.convertAndSend(ALLOCATION_FAILURE_QUEUE, AllocationFailureEvent.builder()
                .orderId(UUID.fromString(beerOrderId))
                .build());
        log.debug("Sent Allocation Failure Event to queue '" + ALLOCATION_FAILURE_QUEUE + "' for order id {}", beerOrderId);
    }
}
