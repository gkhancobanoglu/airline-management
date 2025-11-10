package com.cobanoglu.airlinemanagement.exception;

public class OverbookingException extends RuntimeException {
    public OverbookingException(String message) {
        super(message);
    }
}
