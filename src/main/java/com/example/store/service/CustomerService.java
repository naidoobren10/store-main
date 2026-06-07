package com.example.store.service;

import com.example.store.dto.CreateCustomerRequestDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerDTO createCustomer(CreateCustomerRequestDTO request) {
        log.info("Creating customer with name='{}'", request.getName());
        Customer customer = new Customer();
        customer.setName(request.getName());
        CustomerDTO customerDTO = customerMapper.customerToCustomerDTO(customerRepository.save(customer));
        log.info("Created customer with id={}", customerDTO.getId());
        return customerDTO;
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findCustomers(String nameSearchQueryString) {
        log.info("Searching customers with queryString='{}'", nameSearchQueryString);
        List<Customer> customers;

        if (nameSearchQueryString == null || nameSearchQueryString.isBlank()) {
            customers = customerRepository.findAll();
        } else {
            String queryStringPattern = generateQueryStringSearchPattern(nameSearchQueryString.trim());
            log.debug("Using customer search pattern='{}'", queryStringPattern);
            customers = customerRepository.findByNameContainingQueryString(queryStringPattern);
        }

        List<CustomerDTO> customerDTOs = customerMapper.customersToCustomerDTOs(customers);
        log.info("Found {} customers for queryString='{}'", customerDTOs.size(), nameSearchQueryString);
        return customerDTOs;
    }

    private String generateQueryStringSearchPattern(String queryString) {
        String[] parts = queryString.toLowerCase().split("\\s+");

        return "^"
                + Arrays.stream(parts)
                        .map(part -> escapePostgresRegexToken(part))
                        .map(part -> part + "[^[:space:]]*")
                        .collect(Collectors.joining("\\s+"))
                + "(\\s+.*)?$";
    }

    private String escapePostgresRegexToken(String token) {
        StringBuilder escapedToken = new StringBuilder(token.length());

        for (char character : token.toCharArray()) {
            if ("\\.^$|?*+()[]{}".indexOf(character) >= 0) {
                escapedToken.append('\\');
            }

            escapedToken.append(character);
        }

        return escapedToken.toString();
    }
}
