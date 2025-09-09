package com.example.logging_service.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(indexName = "ratelimitlog")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitLog {
    @Id
    private String id= String.valueOf(UUID.randomUUID());
    @Field(type = FieldType.Date)
    private LocalDateTime timeStamp;
    private int currentTokens;
}
