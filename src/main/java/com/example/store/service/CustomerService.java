package com.example.store.service;

import com.example.store.dto.CreateCustomerRequestDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository,
                           CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerDTO createCustomer(CreateCustomerRequestDTO request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }

    public List<CustomerDTO> findCustomers(String nameSearchQueryString) {
        List<Customer> customers;

        if (nameSearchQueryString == null || nameSearchQueryString.isBlank()) {
            customers = customerRepository.findAll();
        } else {
            customers = customerRepository.findByNameContainingIgnoreCase(nameSearchQueryString);
        }

        return customerMapper.customersToCustomerDTOs(customers);
    }
}
