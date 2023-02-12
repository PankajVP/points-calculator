package com.anymind.points.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuration class for GraphQL related beans.
 */
@Configuration
public class GraphQlConfig {
	/**
	 * Add scalers which are used for date and Price and points calculation of our schema.
	 *
	 * @return The runtime wiring configuration.
	 */
	@Bean
	public RuntimeWiringConfigurer runtimeWiringConfigurer() {
		return wiringBuilder -> wiringBuilder
				.scalar(ExtendedScalars.DateTime)
				.scalar(ExtendedScalars.GraphQLBigDecimal);
	}
}