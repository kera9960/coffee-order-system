package com.example.coffeeordersystem.domain.point.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
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
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.customer.entity.Customer;
import com.example.coffeeordersystem.domain.customer.repository.CustomerRepository;
import com.example.coffeeordersystem.domain.point.dto.ChargePointRequest;
import com.example.coffeeordersystem.domain.point.dto.ChargePointResponse;
import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;
import com.example.coffeeordersystem.domain.point.repository.PointTransactionRepository;

import jakarta.persistence.LockModeType;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void findByIdForUpdateUsesPessimisticWriteLock() throws Exception {
        Method method = CustomerRepository.class.getMethod("findByIdForUpdate", Long.class);
        Lock lock = method.getAnnotation(Lock.class);

        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }

    @Test
    void chargePointUsesLockedCustomerAndStoresChargeTransactionInOneTransaction() throws Exception {
        Method method = PointService.class.getMethod("chargePoint", Long.class, ChargePointRequest.class);
        Transactional transactional = method.getAnnotation(Transactional.class);
        Customer customer = new Customer("홍길동", 12000L);
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(pointTransactionRepository.save(any(PointTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargePointResponse response = pointService.chargePoint(1L, new ChargePointRequest(5000L));

        ArgumentCaptor<PointTransaction> transactionCaptor = ArgumentCaptor.forClass(PointTransaction.class);
        verify(customerRepository).findByIdForUpdate(1L);
        verify(customerRepository, never()).findById(1L);
        verify(pointTransactionRepository).save(transactionCaptor.capture());
        assertEquals(false, transactional.readOnly());
        assertEquals(17000L, customer.getPointBalance());
        assertEquals(PointTransactionType.CHARGE, transactionCaptor.getValue().getType());
        assertEquals(5000L, transactionCaptor.getValue().getAmount());
        assertEquals(17000L, transactionCaptor.getValue().getBalanceAfter());
        assertEquals(17000L, response.balanceAfter());
    }

    @Test
    void chargePointFailsBeforeSavingTransactionWhenCustomerDoesNotExist() {
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pointService.chargePoint(1L, new ChargePointRequest(5000L))
        );

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, exception.getErrorCode());
        verify(pointTransactionRepository, never()).save(any());
    }

    @Test
    void getPointBalanceReturnsCurrentBalance() {
        Customer customer = new Customer("홍길동", 12000L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertEquals(12000L, pointService.getPointBalance(1L).pointBalance());
    }

    @Test
    void getPointTransactionsChecksCustomerAndUsesTypeFilterWhenTypeExists() {
        PageRequest pageable = PageRequest.of(0, 20);
        Customer customer = new Customer("홍길동", 12000L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(pointTransactionRepository.findAllByCustomerIdAndType(eq(1L), eq(PointTransactionType.CHARGE), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        pointService.getPointTransactions(1L, PointTransactionType.CHARGE, pageable);

        verify(pointTransactionRepository).findAllByCustomerIdAndType(1L, PointTransactionType.CHARGE, pageable);
    }

    @Test
    void getPointTransactionsUsesAllTransactionsWhenTypeDoesNotExist() {
        PageRequest pageable = PageRequest.of(0, 20);
        Customer customer = new Customer("홍길동", 12000L);
        PointTransaction transaction = PointTransaction.createCharge(customer, 5000L, 17000L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(pointTransactionRepository.findAllByCustomerId(1L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(transaction), pageable, 1));

        assertEquals(1, pointService.getPointTransactions(1L, null, pageable).getTotalElements());

        verify(pointTransactionRepository).findAllByCustomerId(1L, pageable);
    }
}
