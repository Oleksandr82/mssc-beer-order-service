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
import tech.nautilus.brewery.model.events.ValidateOrderRequest;

import java.util.UUID;

import static tech.nautilus.beer.order.service.config.JmsConfig.VALIDATE_ORDER_REQUEST_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);

        beerOrderRepository.findById(UUID.fromString(beerOrderId)).ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(VALIDATE_ORDER_REQUEST_QUEUE, ValidateOrderRequest.builder()
                    .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                    .build());
            log.debug("Sent Validation request to queue '" + VALIDATE_ORDER_REQUEST_QUEUE + "' for order id {}", beerOrderId);

        }, () -> log.error("Beer Oder Not Found: {}", beerOrderId));

    }
}
