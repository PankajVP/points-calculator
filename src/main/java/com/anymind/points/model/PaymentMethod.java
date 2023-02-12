package com.anymind.points.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;


@Data
@Builder
public class PaymentMethod {
	@Id
	@JsonProperty("id") private Integer id;
	@JsonProperty("name") private String name;
	@JsonProperty("priceModifierFrom") private BigDecimal priceModifierFrom;
	@JsonProperty("priceModifierTo") private BigDecimal priceModifierTo;
	@JsonProperty("pointsModifier") private BigDecimal pointsModifier;
}
