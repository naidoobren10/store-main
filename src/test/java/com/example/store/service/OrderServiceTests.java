package com.example.store.service;

import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private OrderRequestDTO orderRequestDTO;
    private Customer customer;
    private Order savedOrder;
    private Product product;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setDescription("Test Order");
        orderRequestDTO.setCustomerId(1L);
        orderRequestDTO.setProductIds(List.of(100L));

        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        product = new Product();
        product.setId(100L);
        product.setDescription("Test Product");

        savedOrder = new Order();
        savedOrder.setId(10L);
        savedOrder.setDescription(orderRequestDTO.getDescription());
        savedOrder.setCustomer(customer);

        OrderCustomerDTO orderCustomerDTO = new OrderCustomerDTO();
        orderCustomerDTO.setId(customer.getId());
        orderCustomerDTO.setName(customer.getName());

        orderDTO = new OrderDTO();
        orderDTO.setId(savedOrder.getId());
        orderDTO.setDescription(savedOrder.getDescription());
        orderDTO.setCustomer(orderCustomerDTO);
        OrderProductDTO orderProductDTO = new OrderProductDTO();
        orderProductDTO.setId(product.getId());
        orderProductDTO.setDescription(product.getDescription());
        orderDTO.setProducts(List.of(orderProductDTO));
    }

    @Test
    void createOrderSavesOrderWhenCustomerExists() {
        when(customerRepository.findById(orderRequestDTO.getCustomerId())).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(orderRequestDTO.getProductIds())).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.orderToOrderDTO(savedOrder)).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(orderRequestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order orderToSave = orderCaptor.getValue();
        assertEquals("Test Order", orderToSave.getDescription());
        assertSame(customer, orderToSave.getCustomer());
        assertEquals(List.of(product), orderToSave.getProducts());
        assertSame(orderDTO, result);
    }

    @Test
    void createOrderThrowsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(orderRequestDTO.getCustomerId())).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Customer not found: 1", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderThrowsWhenProductDoesNotExist() {
        when(customerRepository.findById(orderRequestDTO.getCustomerId())).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(orderRequestDTO.getProductIds())).thenReturn(List.of());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("One or more products could not be found.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderThrowsWhenDuplicateProductIdsAreProvided() {
        orderRequestDTO.setProductIds(List.of(100L, 100L));

        when(customerRepository.findById(orderRequestDTO.getCustomerId())).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderRequestDTO));

        assertEquals("Duplicate product IDs are not allowed.", exception.getMessage());
        verify(productRepository, never()).findAllById(orderRequestDTO.getProductIds());
    }

    @Test
    void getAllOrdersReturnsMappedOrders() {
        List<Order> orders = List.of(savedOrder);
        Page<Order> orderPage = new PageImpl<>(orders);
        PageRequest pageRequest = PageRequest.of(0, 50);

        when(orderRepository.findAll(pageRequest)).thenReturn(orderPage);
        when(orderMapper.orderToOrderDTO(savedOrder)).thenReturn(orderDTO);

        Page<OrderDTO> result = orderService.getAllOrders(pageRequest);

        assertEquals(List.of(orderDTO), result.getContent());
    }

    @Test
    void getOrderByIdReturnsOrder() {
        Order order = savedOrder;

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.getOrderByID(orderDTO.getId());
        assertSame(orderDTO, result);
    }

    @Test
    void getOrderByIdThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderByID(1L));

        assertEquals("Order not found: 1", exception.getMessage());
        verify(orderRepository).findById(1L);
    }
}
