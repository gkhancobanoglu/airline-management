package com.cobanoglu.airlinemanagement.dto;

import com.cobanoglu.airlinemanagement.entity.CardOfBrand;
import com.cobanoglu.airlinemanagement.entity.SecureType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CardInfoDTO {

    @Enumerated(EnumType.STRING)
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

    @Enumerated(EnumType.STRING)
    @NotNull
    private SecureType secureType;
}
