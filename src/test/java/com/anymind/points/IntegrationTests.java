package com.anymind.points;

import com.anymind.points.dto.SaleResponseDto;
import com.anymind.points.dto.SalesListDto;
import com.anymind.points.repository.SalesRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * WARNING: AS NO MOCKS ARE USED, EXECUTING THE INTEGRATION TEST WILL DELETE ALL THE EXISTING DATA IN POSTGRESQL
 * UNCOMMENT THE CLASS TO RUN
 */
/*

@AutoConfigureGraphQlTester
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests {
	@Autowired
	private GraphQlTester graphQlTester;
	@Autowired
	DatabaseClient database;
	@Autowired
	private SalesRepository salesRepository;

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	@Order(1)
	public void setUp() {
		Hooks.onOperatorDebug();

		database.sql("DELETE FROM sale;").fetch()
				.rowsUpdated()
				.as(StepVerifier::create)
				.expectNextCount(1)
				.verifyComplete();

	}


	@Test
	@Order(2)
	void shouldCreateNewSale() {
		int currentSaleCount = salesRepository.findAll().collectList().block().size();

		// language=GraphQL
		String document = """
				    mutation {
						addSale(salesInput:{
							 price:100
							 priceModifier:0.95
							 paymentMethod:"CASH"
							 datetime:"2022-09-01T00:00:00Z"
						 }){
							 finalPrice,
							 points
						 }
				    }
				""";

		graphQlTester.document(document)
				.execute()
				.path("addSale")
				.entity(SaleResponseDto.class)
				.satisfies(saleResponseDto -> {
					assertThat(BigDecimal.valueOf(95), Matchers.comparesEqualTo(saleResponseDto.getFinalPrice()));
					assertThat(BigDecimal.valueOf(5), Matchers.comparesEqualTo(saleResponseDto.getPoints()));
				});


		document = """
				query{
				       getSalesByRange(dateRangeInput:{from:"2022-09-01T00:00:00Z"
				        ,to:"2022-09-01T01:00:00Z"}){
				          datetime,
				          sales,
				          points
				      }
				  }
				  """;

		graphQlTester.document(document)
				.execute()
				.path("getSalesByRange")
				.entityList(SalesListDto.class)
				.satisfies(salesListDtoList -> {
					SalesListDto salesListDto=salesListDtoList.get(0);
					assertThat(ZonedDateTime.parse("2022-09-01T00:00Z[UTC]"), Matchers.comparesEqualTo(salesListDto.getDatetime()));
					assertThat(BigDecimal.valueOf(95), Matchers.comparesEqualTo(salesListDto.getSales()));
					assertThat(BigDecimal.valueOf(5), Matchers.comparesEqualTo(salesListDto.getPoints()));
				});

		assertEquals((currentSaleCount + 1), salesRepository.findAll().collectList().block().size());
	}

}
 */