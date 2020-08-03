package tech.nautilus.beer.order.service.sm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;
import tech.nautilus.beer.order.service.domain.BeerOrderEventEnum;
import tech.nautilus.beer.order.service.domain.BeerOrderStatusEnum;

@Slf4j
@Component
public class BeerOrderStateMachineListener extends StateMachineListenerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public void stateChanged(State<BeerOrderStatusEnum, BeerOrderEventEnum> from, State<BeerOrderStatusEnum, BeerOrderEventEnum> to) {
        log.info(String.format("stateChanged (from: %s, to: %s)", from, to));
    }
}
