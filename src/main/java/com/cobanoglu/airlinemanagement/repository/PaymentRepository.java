package com.cobanoglu.airlinemanagement.repository;

import com.cobanoglu.airlinemanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySession(String session);
    Optional<Payment> findByOrderId(String orderId);
}
