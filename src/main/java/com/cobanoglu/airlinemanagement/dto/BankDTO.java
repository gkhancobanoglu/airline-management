package com.cobanoglu.airlinemanagement.dto;

import com.cobanoglu.airlinemanagement.entity.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankDTO {

    @NotNull
    private CardOfBrand cardOfBrand;

    @NotNull
    private String cardHolderName;

    @NotNull
    private String pan;

    @NotNull
    private String expiry;

    @NotNull
    private int cvv;

    @NotNull
    private int numberOfInstallments;

    @NotNull
    private String session;

    @NotNull
    private SecureType secureType;

    @NotNull
    private BigDecimal currencyAmount;

    @NotNull
    private Currency currency;

    @NotNull
    private String orderId;

    @NotNull
    private String orderDescription;

    private List<Item> orderItems;

    @NotNull
    private Status status;
}
