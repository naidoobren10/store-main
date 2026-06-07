package com.example.store.controller;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.service.OrderService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(@PageableDefault(size = 50) Pageable pageable) {
        log.info("Received get orders request for page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        log.info(
                "Returning page {} of {} with {} orders",
                orders.getNumber(),
                orders.getTotalPages(),
                orders.getNumberOfElements());
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        log.info(
                "Received create order request for customerId={} with {} products",
                request.getCustomerId(),
                request.getProductIds().size());
        OrderDTO orderDTO = orderService.createOrder(request);
        log.info("Returning created order with id={}", orderDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderByID(@PathVariable Long orderId) {
        log.info("Received get order request for id={}", orderId);
        OrderDTO orderDTO = orderService.getOrderByID(orderId);
        log.info("Returning order with id={}", orderDTO.getId());
        return ResponseEntity.status(HttpStatus.OK).body(orderDTO);
    }
}
