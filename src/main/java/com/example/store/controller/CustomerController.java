package com.example.store.controller;

import com.example.store.dto.CreateCustomerRequestDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(
            @RequestParam(required = false) String queryString
    ) {
        log.info("Received customer search request with queryString='{}'", queryString);
        List<CustomerDTO> customerDTOList = customerService.findCustomers(queryString);
        log.info("Returning {} customers for queryString='{}'", customerDTOList.size(), queryString);
        return ResponseEntity.status(HttpStatus.OK).body(customerDTOList);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerRequestDTO request) {
        log.info("Received create customer request for name='{}'", request.getName());
        CustomerDTO customerDTO = customerService.createCustomer(request);
        log.info("Returning created customer with id={}", customerDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(customerDTO);

    }
}
