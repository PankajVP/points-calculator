package com.anymind.points.dto;

import com.anymind.points.model.PaymentMethod;
import com.anymind.points.model.SalesInput;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class RequestData {
	Integer salesId;
	SalesInput salesInput;
	PaymentMethod paymentMethod;
}
