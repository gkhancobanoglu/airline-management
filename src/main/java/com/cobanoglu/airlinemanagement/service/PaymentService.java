package com.cobanoglu.airlinemanagement.service;

import com.cobanoglu.airlinemanagement.dto.CardInfoDTO;
import com.cobanoglu.airlinemanagement.entity.Payment;

public interface PaymentService {

    Payment processPayment(CardInfoDTO dto);
}
