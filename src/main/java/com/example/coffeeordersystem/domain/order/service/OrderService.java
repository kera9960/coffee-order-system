package com.example.coffeeordersystem.domain.order.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
import com.example.coffeeordersystem.domain.order.dto.OrderItemResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderSummaryResponse;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.repository.PointTransactionRepository;
import com.example.coffeeordersystem.domain.point.service.PointService;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final PointService pointService;
    private final PointTransactionRepository pointTransactionRepository;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            PointService pointService,
            PointTransactionRepository pointTransactionRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.pointService = pointService;
        this.pointTransactionRepository = pointTransactionRepository;
    }

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        validateOrderItems(request.items());
        validateDuplicatedMenuIds(request.items());

        Customer customer = findCustomerForUpdate(customerId);
        Map<Long, Menu> menusById = findMenusById(request.items());
        validateAllMenusExist(request.items(), menusById);

        List<OrderItem> orderItems = request.items().stream()
                .map(item -> createOrderItem(item, menusById.get(item.menuId())))
                .toList();
        Order order = new Order(customer, orderItems);
        pointService.usePointForOrder(customer, order.getTotalAmount());
        Order savedOrder = orderRepository.saveAndFlush(order);
        PointTransaction pointTransaction = pointService.recordUseTransaction(customer, savedOrder.getId(), savedOrder.getTotalAmount());
        return OrderResponse.of(savedOrder, pointTransaction);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrders(Long customerId, Pageable pageable) {
        validateCustomerExists(customerId);
        return orderRepository.findSummariesByCustomerId(customerId, pageable)
                .map(OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long customerId, Long orderId) {
        validateCustomerExists(customerId);
        Order order = orderRepository.findDetailByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        PointTransaction pointTransaction = pointTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return OrderDetailResponse.of(order, pointTransaction);
    }

    private void validateOrderItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_ORDER_ITEMS);
        }
        if (items.stream().anyMatch(item -> item.quantity() == null || item.quantity() < 1)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }
        if (items.stream().anyMatch(item -> item.menuId() == null)) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
    }

    private void validateDuplicatedMenuIds(List<OrderItemRequest> items) {
        Set<Long> menuIds = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!menuIds.add(item.menuId())) {
                throw new BusinessException(ErrorCode.DUPLICATED_ORDER_MENU);
            }
        }
    }

    private Customer findCustomerForUpdate(Long customerId) {
        return customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private void validateCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
    }

    private Map<Long, Menu> findMenusById(List<OrderItemRequest> items) {
        Set<Long> menuIds = items.stream()
                .map(OrderItemRequest::menuId)
                .collect(Collectors.toSet());
        return menuRepository.findAllByIdIn(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));
    }

    private void validateAllMenusExist(List<OrderItemRequest> items, Map<Long, Menu> menusById) {
        boolean allMenusExist = items.stream()
                .map(OrderItemRequest::menuId)
                .allMatch(menusById::containsKey);
        if (!allMenusExist) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
    }

    private OrderItem createOrderItem(OrderItemRequest item, Menu menu) {
        if (menu.getStatus() != MenuStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.MENU_NOT_ON_SALE);
        }
        return OrderItem.from(menu, item.quantity());
    }
}
