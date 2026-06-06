package com.example.store.service;

import com.example.store.dto.CreateCustomerRequestDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private CreateCustomerRequestDTO request;
    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        request = new CreateCustomerRequestDTO();
        request.setName("John Doe");

        customer = new Customer();
        customer.setId(1L);
        customer.setName(request.getName());

        customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId());
        customerDTO.setName(customer.getName());
    }

    @Test
    void createCustomerSavesMappedCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        CustomerDTO result = customerService.createCustomer(request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());

        Customer customerToSave = customerCaptor.getValue();
        assertEquals("John Doe", customerToSave.getName());
        assertSame(customerDTO, result);
    }

    @Test
    void findCustomersReturnsMappedCustomers() {
        List<Customer> customers = List.of(customer);
        List<CustomerDTO> customerDTOs = List.of(customerDTO);

        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.customersToCustomerDTOs(customers)).thenReturn(customerDTOs);

        List<CustomerDTO> result = customerService.findCustomers("");

        assertSame(customerDTOs, result);
    }

    @Test
    void findCustomerByNameSearch() {
        List<Customer> customers = new ArrayList<>();
        Customer newCustomer = new Customer();
        newCustomer.setId(2L);
        newCustomer.setName("Joe Doe");
        customers.add(customer);
        customers.add(newCustomer);

        List<CustomerDTO> customerDTOs = new ArrayList<>();
        CustomerDTO newCustomerDTO = new CustomerDTO();
        newCustomerDTO.setName(newCustomer.getName());
        newCustomerDTO.setId(newCustomer.getId());
        customerDTOs.add(customerDTO);
        customerDTOs.add(newCustomerDTO);

        String pattern = "^jo[^[:space:]]*(\\s+.*)?$";

        when(customerRepository.findByNameContainingQueryString(pattern)).thenReturn(customers);
        when(customerMapper.customersToCustomerDTOs(customers)).thenReturn(customerDTOs);

        List<CustomerDTO> result = customerService.findCustomers("Jo");

        assertSame(customerDTOs, result);
        verify(customerRepository).findByNameContainingQueryString(pattern);
    }

    @Test
    void findCustomersReturnsEmptyListWhenSearchHasNoMatches() {
        List<Customer> customers = List.of();
        List<CustomerDTO> customerDTOs = List.of();

        String pattern = "^missing[^[:space:]]*(\\s+.*)?$";

        when(customerRepository.findByNameContainingQueryString(pattern)).thenReturn(customers);
        when(customerMapper.customersToCustomerDTOs(customers)).thenReturn(customerDTOs);

        List<CustomerDTO> result = customerService.findCustomers("Missing");

        assertSame(customerDTOs, result);
        verify(customerRepository).findByNameContainingQueryString(pattern);
    }
}
