package com.example.store.controller;

import com.example.store.dto.CustomerDTO;
import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.error.CustomerNotFoundException;
import com.example.store.error.OrderNotFoundException;
import com.example.store.error.ProductNotFoundException;
import com.example.store.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderCustomerDTO orderCustomerDTO;
    private CustomerDTO customerDTO;
    private OrderDTO orderDTO;
    private OrderRequestDTO orderRequestDTO;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setId(1L);

        orderCustomerDTO = new OrderCustomerDTO();
        orderCustomerDTO.setId(customerDTO.getId());
        orderCustomerDTO.setName(customerDTO.getName());

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setDescription("Test Order");
        orderDTO.setCustomer(orderCustomerDTO);

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setDescription(orderDTO.getDescription());
        orderRequestDTO.setCustomerId(orderDTO.getCustomer().getId());
        orderRequestDTO.setProductIds(List.of(100L));

        OrderProductDTO orderProductDTO = new OrderProductDTO();
        orderProductDTO.setId(100L);
        orderProductDTO.setDescription("Test Product");
        orderDTO.setProducts(List.of(orderProductDTO));

    }

    @Test
    void testCreateOrder() throws Exception {


        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"))
                .andExpect(jsonPath("$.products[0].description").value("Test Product"));
    }

    @Test
    void testCreateOrderWhenCustomerNotFound() throws Exception {
        when(orderService.createOrder(any(OrderRequestDTO.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found: " + orderRequestDTO.getCustomerId()));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Customer not found"))
                .andExpect(jsonPath("$.detail").value("The customer could not be found."));
    }

    @Test
    void testCreateOrderWhenDescriptionMissing() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"productIds\":[100]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void testCreateOrderWhenDescriptionBlank() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\",\"customerId\":1,\"productIds\":[100]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void testCreateOrderWhenCustomerIdMissing() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test Order\",\"productIds\":[100]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void testCreateOrderWhenProductIdsMissing() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test Order\",\"customerId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void testCreateOrderWhenJsonMalformed() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test Order\",\"customerId\":1,\"productIds\":[100]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"))
                .andExpect(jsonPath("$.detail").value("The request body could not be read."));
    }

    @Test
    void testCreateOrderWhenProductNotFound() throws Exception {
        when(orderService.createOrder(any(OrderRequestDTO.class)))
                .thenThrow(new ProductNotFoundException("One or more products could not be found."));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.detail").value("One or more products could not be found."));
    }

    @Test
    void testCreateOrderWhenDuplicateProductIdsProvided() throws Exception {
        when(orderService.createOrder(any(OrderRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Duplicate product IDs are not allowed."));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("Duplicate product IDs are not allowed."));
    }

    @Test
    void testGetOrder() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Test Order"))
                .andExpect(jsonPath("$[0].customer.name").value("John Doe"))
                .andExpect(jsonPath("$[0].products[0].description").value("Test Product"));
    }

   @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrderByID(1L)).thenReturn(orderDTO);

        mockMvc.perform(get("/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"))
                .andExpect(jsonPath("$.products[0].description").value("Test Product"));
    }

    @Test
    void testGetOrderByIdWhenOrderNotFound() throws Exception {
        when(orderService.getOrderByID(1L)).thenThrow(new OrderNotFoundException("Order not found: 1"));

        mockMvc.perform(get("/order/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"))
                .andExpect(jsonPath("$.detail").value("The order could not be found."));
    }
}
