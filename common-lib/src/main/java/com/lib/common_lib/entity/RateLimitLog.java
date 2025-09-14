package com.lib.common_lib.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
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
    private LocalDate timeStamp;
    private int currentTokens;
    private int maxBucketSize;
    private boolean isAllowed;
    private int refileRate;
}
