package com.example.store.service;

import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.error.CustomerNotFoundException;
import com.example.store.error.OrderNotFoundException;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
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
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private OrderRequestDTO orderRequestDTO;
    private Customer customer;
    private Order savedOrder;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setDescription("Test Order");
        orderRequestDTO.setCustomerId(1L);

        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

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
    }

    @Test
    void createOrderSavesOrderWhenCustomerExists() {
        when(customerRepository.findById(orderRequestDTO.getCustomerId())).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.orderToOrderDTO(savedOrder)).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(orderRequestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order orderToSave = orderCaptor.getValue();
        assertEquals("Test Order", orderToSave.getDescription());
        assertSame(customer, orderToSave.getCustomer());
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
    void getAllOrdersReturnsMappedOrders() {
        List<Order> orders = List.of(savedOrder);
        List<OrderDTO> orderDTOs = List.of(orderDTO);

        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.ordersToOrderDTOs(orders)).thenReturn(orderDTOs);

        List<OrderDTO> result = orderService.getAllOrders();

        assertSame(orderDTOs, result);
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
