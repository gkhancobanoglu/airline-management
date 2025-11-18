package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.entity.Payment;
import com.cobanoglu.airlinemanagement.entity.Status;
import com.cobanoglu.airlinemanagement.exception.NotFoundException;
import com.cobanoglu.airlinemanagement.repository.PaymentRepository;
import com.cobanoglu.airlinemanagement.service.PaymentSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentSessionServiceImpl implements PaymentSessionService {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment createSession(Payment payment) {
        payment.setOrderId(UUID.randomUUID().toString());
        payment.setSession(UUID.randomUUID().toString());
        payment.setStatus(Status.Waiting);
        payment.setExpiry(LocalDateTime.now().plusMinutes(2).toString());
        return paymentRepository.save(payment);
    }


    @Override
    public Payment validateSession(String session) {
        Payment payment = paymentRepository.findBySession(session)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if(payment.getExpiry().isBlank()) {
            throw new NotFoundException("Expired session");
        }

        return payment;
    }
}
