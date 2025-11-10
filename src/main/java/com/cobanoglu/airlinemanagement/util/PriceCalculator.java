package com.cobanoglu.airlinemanagement.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PriceCalculator {

    public BigDecimal calculatePrice(BigDecimal basePrice, double occupancyRate) {
        if(occupancyRate <= 0.5){
            return basePrice;
        } else if(occupancyRate < 0.8){
            return basePrice.multiply(BigDecimal.valueOf(1.2));
        } else{
            return basePrice.multiply(BigDecimal.valueOf(1.5));
        }
    }
}
