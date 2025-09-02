package com.rate.limit.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitResponse implements Serializable {
    private LocalDateTime timeStamp;
    private int currentTokens;
    private boolean allowed;

    public RateLimitResponse(int currentTokens,boolean allowed,LocalDateTime timeStamp){
        this.setAllowed(allowed);
        this.setCurrentTokens(currentTokens);
        this.setTimeStamp(timeStamp);
    }

}
