package tech.nautilus.beer.order.service.sm;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import tech.nautilus.beer.order.service.domain.BeerOrderEventEnum;
import tech.nautilus.beer.order.service.domain.BeerOrderStatusEnum;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states) throws Exception {
        states.withStates()
                .initial(BeerOrderStatusEnum.NEW)
                .states(EnumSet.allOf(BeerOrderStatusEnum.class))
                .end(BeerOrderStatusEnum.PICKED_UP)
                .end(BeerOrderStatusEnum.DELIVERED)
                .end(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                .end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION)
                .end(BeerOrderStatusEnum.DELIVERY_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions) throws Exception {
        transitions.withExternal()
                .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.NEW).event(BeerOrderEventEnum.VALIDATE_ORDER)
                .and().withExternal()
                .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATION_EXCEPTION).event(BeerOrderEventEnum.VALIDATION_FAILED)
                .and().withExternal()
                .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATED).event(BeerOrderEventEnum.VALIDATION_PASSED)


                .and().withExternal()
                .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_FAILED)
                .and().withExternal()
                .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATED).event(BeerOrderEventEnum.ALLOCATION_SUCCESS)
                .and().withExternal()
                .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
                .and().withExternal()
                .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.DELIVERY_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
                .and().withExternal()
                .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.DELIVERED).event(BeerOrderEventEnum.BEER_ORDER_PICKED_UP)
                .and().withExternal()
                .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.PICKED_UP).event(BeerOrderEventEnum.BEER_ORDER_PICKED_UP);

    }
}
