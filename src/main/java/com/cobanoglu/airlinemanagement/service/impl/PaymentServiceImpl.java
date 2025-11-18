package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.BankDTO;
import com.cobanoglu.airlinemanagement.dto.CardInfoDTO;
import com.cobanoglu.airlinemanagement.entity.Payment;
import com.cobanoglu.airlinemanagement.repository.PaymentRepository;
import com.cobanoglu.airlinemanagement.service.BankService;
import com.cobanoglu.airlinemanagement.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BankService bankService;

    @Override
    public Payment processPayment(CardInfoDTO dto) {

        Payment payment = paymentRepository.findBySession(dto.getSession())
                .orElseThrow(() -> new RuntimeException(("Invalid session")));

        validateSession(payment);

        payment.setCardOfBrand(dto.getCardOfBrand());
        payment.setCardHolderName(maskName(dto.getCardHolderName()));
        payment.setPan(maskPan(dto.getPan()));
        payment.setExpiry(dto.getExpiry());
        payment.setCvv(dto.getCvv());
        payment.setNumberOfInstallments(dto.getNumberOfInstallments());
        payment.setSecureType(dto.getSecureType());

        BankDTO bankDTO = convertToBankTO(payment, dto);

        BankDTO response = bankService.process(bankDTO);
        payment.setStatus(response.getStatus());

        return paymentRepository.save(payment);
    }

    private void validateSession(Payment payment) {
        try {
            var expiresAt = LocalDateTime.parse(payment.getExpiry());
            if (expiresAt.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Session expired");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid session expiry");
        }
    }

    private String maskName(String name) {
        String[] part = name.split(" ");
        return part[0].substring(0,2) + "*** " + part[1].substring(0,2) + "**";
    }

    private String maskPan(String pan) {
        return pan.substring(0,4) + " **** **** " + pan.substring(pan.length() - 4);
    }

    private BankDTO convertToBankTO(Payment p, CardInfoDTO dto) {
        BankDTO bank = new BankDTO();
        bank.setCardOfBrand(dto.getCardOfBrand());
        bank.setCardHolderName(dto.getCardHolderName());
        bank.setPan(dto.getPan());
        bank.setExpiry(dto.getExpiry());
        bank.setCvv(dto.getCvv());
        bank.setNumberOfInstallments(dto.getNumberOfInstallments());
        bank.setSecureType(dto.getSecureType());
        bank.setCurrencyAmount(p.getCurrencyAmount());
        bank.setCurrency(p.getCurrency());
        bank.setOrderId(p.getOrderId());
        bank.setOrderDescription(p.getOrderDescription());
        bank.setOrderItems(p.getOrderItem());
        bank.setStatus(p.getStatus());
        return bank;
    }
}
