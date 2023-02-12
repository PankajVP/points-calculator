package com.anymind.points.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SaleResponseDto {
    @JsonProperty("finalPrice") BigDecimal finalPrice;
    @JsonProperty("points") BigDecimal points;
}
