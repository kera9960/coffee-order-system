package com.example.coffeeordersystem.domain.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.customer.entity.Customer;
import com.example.coffeeordersystem.domain.customer.repository.CustomerRepository;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.dto.CreateOrderRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderDetailResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderItemRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderSummaryResponse;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderSummaryProjection;
import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.repository.PointTransactionRepository;
import com.example.coffeeordersystem.domain.point.service.PointService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private PointService pointService;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderUsesLockedCustomerBulkMenuLookupAndStoresUseTransaction() throws Exception {
        Method method = OrderService.class.getMethod("createOrder", Long.class, CreateOrderRequest.class);
        Transactional transactional = method.getAnnotation(Transactional.class);
        Customer customer = customer(1L, 20000L);
        Menu americano = menu(1L, "아메리카노", 4500L, MenuStatus.ON_SALE);
        Menu latte = menu(2L, "카페라떼", 5000L, MenuStatus.ON_SALE);
        PointTransaction useTransaction = PointTransaction.createUse(customer, 20L, 14000L, 6000L);
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(menuRepository.findAllByIdIn(any())).thenReturn(List.of(americano, latte));
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 20L);
            return order;
        });
        when(pointService.recordUseTransaction(customer, 20L, 14000L)).thenReturn(useTransaction);

        OrderResponse response = orderService.createOrder(1L, new CreateOrderRequest(List.of(
                new OrderItemRequest(1L, 2),
                new OrderItemRequest(2L, 1)
        )));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(customerRepository, times(1)).findByIdForUpdate(1L);
        verify(customerRepository, never()).findById(1L);
        verify(menuRepository, times(1)).findAllByIdIn(any());
        verify(orderRepository).saveAndFlush(orderCaptor.capture());
        verify(pointService).usePointForOrder(customer, 14000L);
        verify(pointService).recordUseTransaction(customer, 20L, 14000L);
        assertEquals(false, transactional.readOnly());
        assertEquals(14000L, orderCaptor.getValue().getTotalAmount());
        assertEquals(OrderStatus.COMPLETED, orderCaptor.getValue().getStatus());
        assertEquals(2, orderCaptor.getValue().getItems().size());
        assertEquals(14000L, response.totalAmount());
        assertEquals(-14000L, response.pointTransaction().amount());
    }

    @Test
    void createOrderAllowsSingleMenuOrder() {
        Customer customer = customer(1L, 20000L);
        Menu americano = menu(1L, "아메리카노", 4500L, MenuStatus.ON_SALE);
        PointTransaction useTransaction = PointTransaction.createUse(customer, 20L, 4500L, 15500L);
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(menuRepository.findAllByIdIn(any())).thenReturn(List.of(americano));
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 20L);
            return order;
        });
        when(pointService.recordUseTransaction(customer, 20L, 4500L)).thenReturn(useTransaction);

        OrderResponse response = orderService.createOrder(1L, new CreateOrderRequest(List.of(new OrderItemRequest(1L, 1))));

        assertEquals(1, response.items().size());
        assertEquals(4500L, response.totalAmount());
    }

    @Test
    void duplicatedMenuIdFailsWithoutCustomerLookup() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(1L, new CreateOrderRequest(List.of(
                        new OrderItemRequest(1L, 1),
                        new OrderItemRequest(1L, 2)
                )))
        );

        assertEquals(ErrorCode.DUPLICATED_ORDER_MENU, exception.getErrorCode());
        verify(customerRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void stoppedMenuFailsWithoutOrderOrUseTransactionSave() {
        Customer customer = customer(1L, 20000L);
        Menu stopped = menu(1L, "아메리카노", 4500L, MenuStatus.STOPPED);
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(menuRepository.findAllByIdIn(any())).thenReturn(List.of(stopped));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(1L, new CreateOrderRequest(List.of(new OrderItemRequest(1L, 1))))
        );

        assertEquals(ErrorCode.MENU_NOT_ON_SALE, exception.getErrorCode());
        verify(orderRepository, never()).saveAndFlush(any());
        verify(pointService, never()).recordUseTransaction(any(), any(), any());
    }

    @Test
    void missingMenuFailsWithoutOrderOrUseTransactionSave() {
        Customer customer = customer(1L, 20000L);
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(menuRepository.findAllByIdIn(any())).thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(1L, new CreateOrderRequest(List.of(new OrderItemRequest(1L, 1))))
        );

        assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
        verify(orderRepository, never()).saveAndFlush(any());
        verify(pointService, never()).recordUseTransaction(any(), any(), any());
    }

    @Test
    void insufficientPointsFailsBeforeOrderOrUseTransactionSave() {
        Customer customer = customer(1L, 4000L);
        Menu americano = menu(1L, "아메리카노", 4500L, MenuStatus.ON_SALE);
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(menuRepository.findAllByIdIn(any())).thenReturn(List.of(americano));
        org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.INSUFFICIENT_POINTS))
                .when(pointService).usePointForOrder(customer, 4500L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(1L, new CreateOrderRequest(List.of(new OrderItemRequest(1L, 1))))
        );

        assertEquals(ErrorCode.INSUFFICIENT_POINTS, exception.getErrorCode());
        verify(orderRepository, never()).saveAndFlush(any());
        verify(pointService, never()).recordUseTransaction(any(), any(), any());
    }

    @Test
    void getOrdersUsesSummaryProjectionWithoutLoadingItems() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findSummariesByCustomerId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(new TestOrderSummaryProjection()), pageable, 1));

        Page<OrderSummaryResponse> response = orderService.getOrders(1L, pageable);

        verify(orderRepository).findSummariesByCustomerId(1L, pageable);
        assertEquals(2L, response.getContent().get(0).itemCount());
    }

    @Test
    void getOrderReturnsDetailWithUseTransaction() {
        Customer customer = customer(1L, 20000L);
        Order order = new Order(customer, List.of(OrderItem.from(menu(1L, "아메리카노", 4500L, MenuStatus.ON_SALE), 1)));
        PointTransaction transaction = PointTransaction.createUse(customer, 20L, 4500L, 15500L);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findDetailByIdAndCustomerId(20L, 1L)).thenReturn(Optional.of(order));
        when(pointTransactionRepository.findByOrderId(20L)).thenReturn(Optional.of(transaction));

        OrderDetailResponse response = orderService.getOrder(1L, 20L);

        assertEquals(1, response.items().size());
        assertEquals(-4500L, response.pointTransaction().amount());
    }

    @Test
    void getOrderFailsWhenOrderDoesNotBelongToCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findDetailByIdAndCustomerId(20L, 1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> orderService.getOrder(1L, 20L));

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    private Customer customer(Long id, Long pointBalance) {
        Customer customer = new Customer("홍길동", pointBalance);
        ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }

    private Menu menu(Long id, String name, Long price, MenuStatus status) {
        Menu menu = new Menu(name, price);
        ReflectionTestUtils.setField(menu, "id", id);
        menu.changeStatus(status);
        return menu;
    }

    private static class TestOrderSummaryProjection implements OrderSummaryProjection {

        @Override
        public Long getId() {
            return 20L;
        }

        @Override
        public Long getCustomerId() {
            return 1L;
        }

        @Override
        public Long getTotalAmount() {
            return 13500L;
        }

        @Override
        public OrderStatus getStatus() {
            return OrderStatus.COMPLETED;
        }

        @Override
        public LocalDateTime getOrderedAt() {
            return LocalDateTime.of(2026, 7, 15, 10, 40);
        }

        @Override
        public Long getItemCount() {
            return 2L;
        }
    }
}
