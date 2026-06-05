package com.example.store.controller;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;

import com.example.store.service.OrderService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

   public OrderController(OrderService orderService) {
        this.orderService = orderService;
   }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
       List<OrderDTO> orders = orderService.getAllOrders();
       return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder( @Valid @RequestBody OrderRequestDTO request) {
        OrderDTO orderDTO = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderByID( @PathVariable Long orderId) {
        OrderDTO orderDTO = orderService.getOrderByID(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(orderDTO);
    }


}
