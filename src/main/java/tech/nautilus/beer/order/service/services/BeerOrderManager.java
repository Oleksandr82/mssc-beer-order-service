package tech.nautilus.beer.order.service.services;

import tech.nautilus.beer.order.service.domain.BeerOrder;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
}
