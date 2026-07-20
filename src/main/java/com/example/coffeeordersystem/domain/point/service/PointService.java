package com.example.coffeeordersystem.domain.point.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.coffeeordersystem.common.error.BusinessException;
import com.example.coffeeordersystem.common.error.ErrorCode;
import com.example.coffeeordersystem.domain.customer.entity.Customer;
import com.example.coffeeordersystem.domain.customer.repository.CustomerRepository;
import com.example.coffeeordersystem.domain.point.dto.ChargePointRequest;
import com.example.coffeeordersystem.domain.point.dto.ChargePointResponse;
import com.example.coffeeordersystem.domain.point.dto.PointBalanceResponse;
import com.example.coffeeordersystem.domain.point.dto.PointTransactionResponse;
import com.example.coffeeordersystem.domain.point.entity.PointTransaction;
import com.example.coffeeordersystem.domain.point.entity.PointTransactionType;
import com.example.coffeeordersystem.domain.point.repository.PointTransactionRepository;

@Service
public class PointService {

    private final CustomerRepository customerRepository;
    private final PointTransactionRepository pointTransactionRepository;

    public PointService(
            CustomerRepository customerRepository,
            PointTransactionRepository pointTransactionRepository
    ) {
        this.customerRepository = customerRepository;
        this.pointTransactionRepository = pointTransactionRepository;
    }

    @Transactional(readOnly = true)
    public PointBalanceResponse getPointBalance(Long customerId) {
        Customer customer = findCustomer(customerId);
        return new PointBalanceResponse(customer.getId(), customer.getPointBalance());
    }

    @Transactional
    public ChargePointResponse chargePoint(Long customerId, ChargePointRequest request) {
        Customer customer = findCustomerForUpdate(customerId);
        customer.chargePoint(request.amount());
        PointTransaction pointTransaction = PointTransaction.createCharge(
                customer,
                request.amount(),
                customer.getPointBalance()
        );
        return ChargePointResponse.from(pointTransactionRepository.save(pointTransaction));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void usePointForOrder(Customer customer, Long totalAmount) {
        try {
            customer.usePoint(totalAmount);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PointTransaction recordUseTransaction(Customer customer, Long orderId, Long totalAmount) {
        PointTransaction pointTransaction = PointTransaction.createUse(
                customer,
                orderId,
                totalAmount,
                customer.getPointBalance()
        );
        return pointTransactionRepository.save(pointTransaction);
    }

    @Transactional(readOnly = true)
    public Page<PointTransactionResponse> getPointTransactions(
            Long customerId,
            PointTransactionType type,
            Pageable pageable
    ) {
        findCustomer(customerId);
        Page<PointTransaction> transactions = type == null
                ? pointTransactionRepository.findAllByCustomerId(customerId, pageable)
                : pointTransactionRepository.findAllByCustomerIdAndType(customerId, type, pageable);
        return transactions.map(PointTransactionResponse::from);
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private Customer findCustomerForUpdate(Long customerId) {
        return customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }
}
