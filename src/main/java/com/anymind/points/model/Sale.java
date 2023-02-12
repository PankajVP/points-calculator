package com.anymind.points.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
public class Sale {
    @Id private Integer id;
     private BigDecimal finalPrice;
    private BigDecimal points;
    private Integer paymentMethodId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("datetime") @Column(value="datetime")
    private ZonedDateTime dateTime;

}
