package com.example.store.service;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.error.CustomerNotFoundException;
import com.example.store.error.OrderNotFoundException;
import com.example.store.error.ProductNotFoundException;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    public List<OrderDTO> getAllOrders() {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll());
    }

    @Transactional
    public OrderDTO createOrder(OrderRequestDTO request) {
        Order order = new Order();
        order.setDescription(request.getDescription());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found: " + request.getCustomerId()));
        List<Long> productIds = request.getProductIds();

        long distinctCount = productIds.stream().distinct().count();
        if (distinctCount != productIds.size()) {
            throw new IllegalArgumentException("Duplicate product IDs are not allowed.");
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ProductNotFoundException("One or more products could not be found.");
        }

        order.setCustomer(customer);
        order.setProducts(products);

        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }

    public OrderDTO getOrderByID(Long id) {
        return orderMapper.orderToOrderDTO(orderRepository
                .findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id)));
    }
}
