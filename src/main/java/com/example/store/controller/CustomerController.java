package com.example.store.controller;

import com.example.store.dto.CreateCustomerRequestDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(
            @RequestParam(required = false) String queryString
    ) {
        List<CustomerDTO> customerDTOList = customerService.findCustomers(queryString);
        return ResponseEntity.status(HttpStatus.OK).body(customerDTOList);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerRequestDTO request) {
        CustomerDTO customerDTO = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerDTO);

    }
}
