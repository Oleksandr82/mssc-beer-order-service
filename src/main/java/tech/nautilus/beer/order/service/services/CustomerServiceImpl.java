package tech.nautilus.beer.order.service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.nautilus.beer.order.service.domain.Customer;
import tech.nautilus.beer.order.service.repositories.CustomerRepository;
import tech.nautilus.beer.order.service.web.mappers.CustomerMapper;
import tech.nautilus.brewery.model.CustomerPagedList;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    @Override
    public CustomerPagedList listCustomers(Pageable pageable) {

        Page<Customer> customersPage = customerRepository.findAll(pageable);

        return new CustomerPagedList(
                customersPage.stream().map(customerMapper::customerToDto).collect(Collectors.toList()),
                PageRequest.of(customersPage.getPageable().getPageNumber(), customersPage.getPageable().getPageSize()),
                customersPage.getTotalElements());
    }
}
