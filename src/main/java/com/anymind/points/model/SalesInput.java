package com.anymind.points.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Transient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@ToString
@Builder
@Accessors(chain = true)
public class SalesInput {
    @JsonProperty("price") private BigDecimal price;
    @JsonProperty("priceModifier") private BigDecimal priceModifier;
    @JsonProperty("priceModifier") private String paymentMethod;
    @JsonProperty("datetime") private ZonedDateTime datetime;
}
