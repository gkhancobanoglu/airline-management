package com.cobanoglu.airlinemanagement.service;

import com.cobanoglu.airlinemanagement.entity.Payment;

public interface PaymentSessionService {

    Payment createSession(Payment payment);
    Payment validateSession(String session);
}
