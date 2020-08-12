package tech.nautilus.beer.order.service.services;

import org.springframework.data.domain.Pageable;
import tech.nautilus.brewery.model.CustomerPagedList;

public interface CustomerService {

    CustomerPagedList listCustomers(Pageable pageable);
}
