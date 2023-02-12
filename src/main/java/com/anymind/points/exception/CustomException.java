package com.anymind.points.exception;

import graphql.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
	private ErrorType errorType;
	private String message;
}