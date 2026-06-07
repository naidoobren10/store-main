package com.example.store.controller;

import com.example.store.dto.CreateCustomerRequestDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private CustomerDTO customerDTO;

    private CreateCustomerRequestDTO createCustomerRequestDTO;

    @BeforeEach
    void setUp() {

        customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setId(1L);

        createCustomerRequestDTO = new CreateCustomerRequestDTO();
        createCustomerRequestDTO.setName("John Doe");
    }

    @Test
    void testCreateCustomer() throws Exception {
        when(customerService.createCustomer(createCustomerRequestDTO)).thenReturn(customerDTO);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testCreateCustomerWhenNameMissing() throws Exception {
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void testCreateCustomerWhenNameBlank() throws Exception {
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void testCreateCustomerWhenJsonMalformed() throws Exception {
        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"))
                .andExpect(jsonPath("$.detail").value("The request body could not be read."));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        when(customerService.findCustomers(null)).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name").value("John Doe"));
    }

    @Test
    void testGetCustomersByQueryString() throws Exception {
        when(customerService.findCustomers("Jo")).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer").param("queryString", "Jo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void testGetCustomersByQueryStringWhenNoMatches() throws Exception {
        when(customerService.findCustomers("Missing")).thenReturn(List.of());

        mockMvc.perform(get("/customer").param("queryString", "Missing"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
