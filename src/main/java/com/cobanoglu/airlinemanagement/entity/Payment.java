package com.cobanoglu.airlinemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false)
    @Positive
    private BigDecimal currencyAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    private int numberOfInstallments = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardOfBrand cardOfBrand;

    @Column(nullable = false)
    private String pan;

    @Column(nullable = false)
    private String expiry;

    @Column(nullable = false)
    private int cvv;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(unique=true,nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String orderDescription;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Item> orderItem;

    @Column(nullable = false)
    private String successUrl;

    @Column(nullable = false)
    private String failureUrl;

    @Column(nullable = false)
    private String session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SecureType secureType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime sessionExpiresAt;
}
