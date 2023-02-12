package com.anymind.points.config;

import com.anymind.points.exception.CustomException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

/**
 * CustomExceptionResolver is a component that extends the DataFetcherExceptionResolverAdapter class
 * to resolve exceptions thrown during GraphQL data fetching. It overrides the resolveToSingleError method
 * to create a GraphQLError object that represents the exception, if it is a CustomException.
 * [EXTRA] Why this class is necessary?
 * Neatly Explained in the following article-
 * https://www.baeldung.com/spring-graphql-error-handling
 */
@Component
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {
	/**
	 * Resolves the given exception into a GraphQLError object that represents the exception,if the exception is a CustomException.
	 *
	 * @param ex  the exception to resolve
	 * @param env the environment for the invoked {@code DataFetcher}
	 * @return a GraphQLError object that represents the CustomException, or null if the exception is not a CustomException
	 */
	@Override
	protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
		if (ex instanceof CustomException) {
			CustomException exception = (CustomException) ex;
			return GraphqlErrorBuilder.newError()
					.errorType(exception.getErrorType())
					.message(exception.getMessage())
					.path(env.getExecutionStepInfo().getPath())
					.location(env.getField().getSourceLocation())
					.build();
		} else return null;
	}
}
