package tech.nautilus.beer.order.service.web.mappers;

import org.mapstruct.Mapper;
import tech.nautilus.beer.order.service.domain.Customer;
import tech.nautilus.brewery.model.CustomerDto;

@Mapper(uses = DateMapper.class)
public interface CustomerMapper {

    CustomerDto customerToDto(Customer customer);
}
