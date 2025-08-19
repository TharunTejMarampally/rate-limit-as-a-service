package com.rate.limit.service.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlgorithmState implements Serializable {

    private String id= UUID.randomUUID().toString();
    private LocalDateTime timeStamp;
    private int refileRate;
    private int maxBucketSize;
    private int currentTokens;
}
