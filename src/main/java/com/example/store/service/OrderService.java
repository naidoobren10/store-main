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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.info("Fetching orders for page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<OrderDTO> orderPage = orderRepository.findAll(pageable).map(orderMapper::orderToOrderDTO);
        log.info(
                "Fetched {} orders for page {} of {}",
                orderPage.getNumberOfElements(),
                orderPage.getNumber(),
                orderPage.getTotalPages());
        return orderPage;
    }

    @Transactional
    public OrderDTO createOrder(OrderRequestDTO request) {
        log.info(
                "Creating order for customerId={} with {} requested products",
                request.getCustomerId(),
                request.getProductIds().size());
        Order order = new Order();
        order.setDescription(request.getDescription());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> {
                    log.warn("Customer not found for order creation: customerId={}", request.getCustomerId());
                    return new CustomerNotFoundException("Customer not found: " + request.getCustomerId());
                });
        List<Long> productIds = request.getProductIds();

        long distinctCount = productIds.stream().distinct().count();
        if (distinctCount != productIds.size()) {
            log.warn("Duplicate product ids supplied for order creation: productIds={}", productIds);
            throw new IllegalArgumentException("Duplicate product IDs are not allowed.");
        }

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            log.warn("Product lookup mismatch for order creation: requested={} found={}", productIds.size(), products.size());
            throw new ProductNotFoundException("One or more products could not be found.");
        }

        order.setCustomer(customer);
        order.setProducts(products);

        OrderDTO orderDTO = orderMapper.orderToOrderDTO(orderRepository.save(order));
        log.info("Created order with id={}", orderDTO.getId());
        return orderDTO;
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderByID(Long id) {
        log.info("Fetching order by id={}", id);
        OrderDTO orderDTO = orderMapper.orderToOrderDTO(orderRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.warn("Order not found for id={}", id);
                    return new OrderNotFoundException("Order not found: " + id);
                }));
        log.info("Fetched order id={}", orderDTO.getId());
        return orderDTO;
    }
}
