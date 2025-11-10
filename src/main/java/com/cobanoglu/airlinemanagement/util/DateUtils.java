package com.cobanoglu.airlinemanagement.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateUtils {

    public boolean isFlightExpired(LocalDateTime departureTime) {
        return departureTime.isBefore(LocalDateTime.now());
    }
}
