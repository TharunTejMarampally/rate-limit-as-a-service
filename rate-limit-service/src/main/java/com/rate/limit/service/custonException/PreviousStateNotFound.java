package com.rate.limit.service.custonException;

public class PreviousStateNotFound extends RuntimeException{
    public PreviousStateNotFound(String message) {
        super(message);
    }
}
