package tech.nautilus.beer.order.service.sm.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.order.service.domain.BeerOrderEventEnum;
import tech.nautilus.beer.order.service.domain.BeerOrderStatusEnum;
import tech.nautilus.beer.order.service.repositories.BeerOrderRepository;
import tech.nautilus.beer.order.service.services.BeerOrderManagerImpl;
import tech.nautilus.beer.order.service.web.mappers.BeerOrderMapper;
import tech.nautilus.brewery.model.events.DeallocateOrderRequest;

import java.util.UUID;

import static tech.nautilus.beer.order.service.config.JmsConfig.DEALLOCATE_ORDER_REQUEST_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);

        beerOrderRepository.findById(UUID.fromString(beerOrderId)).ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(DEALLOCATE_ORDER_REQUEST_QUEUE, DeallocateOrderRequest.builder()
                    .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                    .build());
            log.debug("Sent Deallocate request to queue '" + DEALLOCATE_ORDER_REQUEST_QUEUE + "' for order id {}", beerOrderId);

        }, () -> log.error("Beer Oder Not Found: {}", beerOrderId));
    }
}
