package com.anymind.points.service;

import com.anymind.points.dto.RequestData;
import com.anymind.points.dto.SaleResponseDto;
import com.anymind.points.dto.SalesListDto;
import com.anymind.points.exception.CustomException;
import com.anymind.points.model.DateRangeInput;
import com.anymind.points.model.PaymentMethod;
import com.anymind.points.model.Sale;
import com.anymind.points.model.SalesInput;
import com.anymind.points.repository.PaymentMethodRepository;
import com.anymind.points.repository.SalesRepository;
import graphql.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SalesServiceImplTest {
	@InjectMocks
	private SalesServiceImpl salesService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;
	@Mock
	private SalesRepository salesRepository;

	private Sale mockSale1;
	private Sale mockSale2;
	private SalesListDto salesDto1;
	private SalesListDto salesDto2;
	List<SalesListDto> salesList = new ArrayList<>();

	private SalesInput salesInput;

	private PaymentMethod paymentMethod;

	private RequestData requestData;

	@BeforeEach
	public void before() {
		mockSale1 = Sale.builder()
				.id(1)
				.finalPrice(BigDecimal.valueOf(95.00))
				.points(BigDecimal.valueOf(5))
				.dateTime(ZonedDateTime.now().plusHours(2))
				.paymentMethodId(1)
				.build();

		mockSale2 = Sale.builder()
				.id(2)
				.finalPrice(BigDecimal.valueOf(105.00))
				.points(BigDecimal.valueOf(3))
				.dateTime(ZonedDateTime.now().plusHours(1))
				.paymentMethodId(2)
				.build();

		salesDto1 = SalesListDto.builder().datetime(ZonedDateTime.now().plusHours(2).truncatedTo(ChronoUnit.HOURS)).sales(BigDecimal.valueOf(95.00)).points(BigDecimal.valueOf(5)).build();
		salesDto2 = SalesListDto.builder().datetime(ZonedDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS)).sales(BigDecimal.valueOf(105.00)).points(BigDecimal.valueOf(3)).build();
		salesList.add(salesDto1);
		salesList.add(salesDto2);

		salesInput = SalesInput.builder()
				.price(BigDecimal.valueOf(100))
				.paymentMethod("CASH")
				.priceModifier(BigDecimal.valueOf(0.95))
				.datetime(ZonedDateTime.now())
				.build();

		paymentMethod = PaymentMethod.builder()
				.id(1)
				.name("CASH")
				.pointsModifier(BigDecimal.valueOf(0.05))
				.priceModifierFrom(BigDecimal.valueOf(0.95))
				.priceModifierTo(BigDecimal.valueOf(1.05))
				.build();

		requestData = RequestData.builder()
				.paymentMethod(paymentMethod)
				.salesInput(salesInput)
				.build();

		MockitoAnnotations.initMocks(this);

	}

	@Test
	public void getAllSales_shouldReturnFluxOfSales() {
		//Given
		List<Sale> expectedSales = Arrays.asList(mockSale1, mockSale2);

		when(salesRepository.findAll()).thenReturn(Flux.fromIterable(expectedSales));

		//When
		Flux<Sale> actualSales = salesService.getAllSales();

		//Then
		StepVerifier.create(actualSales)
				.expectNext(mockSale1, mockSale2)
				.verifyComplete();

		verify(salesRepository).findAll();
	}

	@Test
	public void testGetSaleByRange() {

		DateRangeInput dateRangeInput = new DateRangeInput(ZonedDateTime.now(), ZonedDateTime.now().plusHours(4));

		when(salesRepository.findByDateTimeBetweenOrderByDateTime(dateRangeInput.getFrom(), dateRangeInput.getTo()))
				.thenReturn(Flux.fromIterable(Arrays.asList(mockSale1, mockSale2)));

		Flux<SalesListDto> salesListDtoFlux = salesService.getSaleByRange(dateRangeInput);


		StepVerifier.create(salesListDtoFlux)
				.expectNext(salesDto2)
				.expectNext(salesDto1)
				.verifyComplete();
	}


	@Test
	public void testGroupSalesByInterval() {
		DateRangeInput dateRangeInput = new DateRangeInput(ZonedDateTime.now(), ZonedDateTime.now().plusHours(4));

		Mono<Map<ZonedDateTime, List<Sale>>> result = salesService.groupSalesByInterval(Arrays.asList(mockSale1, mockSale2));

		StepVerifier.create(result)
				.assertNext(map -> {
					assertEquals(2, map.size());

					ZonedDateTime key1 = mockSale1.getDateTime().truncatedTo(ChronoUnit.HOURS);
					assertTrue(map.containsKey(key1));

					ZonedDateTime key2 = mockSale2.getDateTime().truncatedTo(ChronoUnit.HOURS);
					assertTrue(map.containsKey(key2));
				})
				.verifyComplete();
	}


	@Test
	public void testGetSalesListDto() {
		mockSale1.setDateTime(ZonedDateTime.now());
		mockSale2.setDateTime(ZonedDateTime.now());

		SalesListDto salesListDto = salesService.getSalesListDto(Map.entry(ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS), Arrays.asList(mockSale1, mockSale2))).block();

		assertEquals(ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS), salesListDto.getDatetime().truncatedTo(ChronoUnit.HOURS));
		assertEquals(BigDecimal.valueOf(200.00), salesListDto.getSales());
		assertEquals(BigDecimal.valueOf(8), salesListDto.getPoints());
	}


	@Test
	public void getSaleByIdTest() {
		Integer id = 1;
		when(salesRepository.findById(id)).thenReturn(Mono.just(mockSale1));

		Mono<Sale> result = salesService.getSaleById(id);

		StepVerifier.create(result)
				.expectNextMatches(sale1 -> sale1.equals(mockSale1))
				.verifyComplete();
	}


	@Test
	public void testAddSale() {
		// when
		SaleResponseDto expectedResponse = SaleResponseDto.builder().finalPrice(BigDecimal.valueOf(95.00)).points(BigDecimal.valueOf(5)).build();

		when(salesRepository.save(any())).thenReturn(Mono.just(mockSale1));
		when(messageSource.getMessage(eq("errormessage.verify-payment-method"), any(), any())).thenReturn("Error Message");
		when(paymentMethodRepository.findByName(anyString())).thenReturn(Mono.just(paymentMethod));

		Mono<SaleResponseDto> actualResponse = salesService.addSale(salesInput);

		// then
		StepVerifier.create(actualResponse)
				.expectNextMatches(saleResponseDto -> {
					assertThat("Check final Price", saleResponseDto.getFinalPrice().compareTo(BigDecimal.valueOf(95)) == 0);
					assertThat("Check Points", saleResponseDto.getPoints().compareTo(BigDecimal.valueOf(5)) == 0);
					return true;
				})
				.verifyComplete();
	}



	@Test
	public void getPriceDto() {
		SaleResponseDto saleResponseDto = salesService.getPriceDto(mockSale1).block();

		assertEquals(saleResponseDto.getFinalPrice(), new BigDecimal("95.00").setScale(BigDecimal.ROUND_HALF_UP));
		assertEquals(saleResponseDto.getPoints(), BigDecimal.valueOf(5));
	}


	@Test
	public void saveSaleTest() {
		when(salesRepository.save(any())).thenReturn(Mono.just(mockSale1));

		Mono<Sale> saleMono = salesService.saveSale(requestData);

		StepVerifier.create(saleMono)
				.assertNext((sale) -> {  // Asserts that the returned sale is correct
					assertEquals(1, sale.getId());  // Asserts that the id is correct
					assertEquals(BigDecimal.valueOf(95.0), sale.getFinalPrice());  // Asserts that the final price is correct
					assertEquals(BigDecimal.valueOf(5), sale.getPoints());  // Asserts that the points are correct
					assertEquals(1, sale.getPaymentMethodId());  // Asserts that the payment method id is correct
					assertNotNull(sale.getDateTime()); // Asserts that the date time is not null

				}).verifyComplete();   // Verifies that there are no more elements in the mono
	}


	@Test
	public void verifyPaymentMethodTest() {
		when(paymentMethodRepository.findByName(anyString())).thenReturn(Mono.just(paymentMethod));

		Mono<RequestData> requestDataMono = salesService.verifyPaymentMethod(salesInput);

		StepVerifier.create(requestDataMono)
				.assertNext(requestData -> {
					assertEquals(requestData.getPaymentMethod().getName(), requestData.getSalesInput().getPaymentMethod());
				})
				.expectComplete()
				.verify();
	}

	@Test
	public void verifyPaymentMethodTestNotFound() {
		when(messageSource.getMessage(eq("errormessage.verify-payment-method"), any(), any())).thenReturn("Invalid Payment Method");
		when(paymentMethodRepository.findByName(anyString())).thenReturn(Mono.just(paymentMethod));

		Mono<RequestData> requestDataMono = salesService.verifyPaymentMethod(salesInput);

		StepVerifier.create(requestDataMono)
				.expectErrorSatisfies(ex -> {
					assertThat("Check type", ex instanceof CustomException);
					CustomException customException = (CustomException) ex;
					assertEquals(customException.getErrorType(), ErrorType.InvalidSyntax);
					assertThat("Check message", customException.getMessage().contains("Invalid Payment Method"));
				}).log();
	}

	@Test
	public void testGetPriceDto() {

		Mono<SaleResponseDto> response = salesService.getPriceDto(mockSale1);

		StepVerifier.create(response)
				.assertNext(saleResponseDto -> {
					assertEquals(BigDecimal.valueOf(95.0).setScale(BigDecimal.ROUND_HALF_UP), saleResponseDto.getFinalPrice());
					assertEquals(BigDecimal.valueOf(5), saleResponseDto.getPoints());
				})
				.verifyComplete();
	}


	@Test
	public void saveSale() {
		when(salesRepository.save(any())).thenReturn(Mono.just(mockSale1));

		Mono<Sale> saleMono = salesService.saveSale(requestData);

		StepVerifier.create(saleMono)
				.assertNext(sale -> {
					assertEquals(BigDecimal.valueOf(95.0), sale.getFinalPrice());
					assertEquals(BigDecimal.valueOf(5), sale.getPoints());
					assertNotNull(sale.getDateTime());
				})
				.verifyComplete();
	}


	@Test
	public void verifyPaymentMethod_whenValidInput_shouldReturnRequestData() {
		when(messageSource.getMessage(eq("errormessage.verify-payment-method"), any(), any())).thenReturn("Error Message");
		when(paymentMethodRepository.findByName(anyString())).thenReturn(Mono.just(paymentMethod));

		StepVerifier.create(salesService.verifyPaymentMethod(salesInput))
				.assertNext(requestData -> {
					assertEquals(paymentMethod, requestData.getPaymentMethod());
					assertEquals(salesInput, requestData.getSalesInput());
				})
				.verifyComplete();

	}


	@Test
	public void verifySaleDataTest() {

		Mono<RequestData> result = salesService.verifySaleData(requestData);

		StepVerifier.create(result)
				.expectNextMatches(data -> data == requestData)
				.verifyComplete();
	}

	@Test
	public void verifyInvalidSaleDataTest() {

		salesInput = SalesInput.builder()
				.price(BigDecimal.valueOf(100))
				.paymentMethod("CASH")
				.priceModifier(BigDecimal.valueOf(1.5)) // INVALID PRICE MODIFIER
				.datetime(ZonedDateTime.now())
				.build();
		when(messageSource.getMessage(any(), any(), any())).thenReturn("Invalid price modifier data for the input::" + salesInput.getPriceModifier());
		requestData.setSalesInput(salesInput);
		Mono<RequestData> result = salesService.verifySaleData(requestData);

		StepVerifier.create(result)
				.expectErrorSatisfies(ex -> {
					assertThat("Check type", ex instanceof CustomException);
					CustomException customException = (CustomException) ex;
					assertEquals(customException.getErrorType(), ErrorType.InvalidSyntax);
					assertThat("Check message", customException.getMessage().contains("Invalid price modifier data for the input"));
				}).log();
	}

	@Test
	public void testUpdateSale() {
		// given
		Integer id = 1;
		// when
		SaleResponseDto expectedResponse = SaleResponseDto.builder().finalPrice(BigDecimal.valueOf(95.00)).points(BigDecimal.valueOf(5)).build();

		when(salesRepository.save(any())).thenReturn(Mono.just(mockSale1));
		when(messageSource.getMessage(eq("errormessage.verify-payment-method"), any(), any())).thenReturn("Error Message");
		when(paymentMethodRepository.findByName(anyString())).thenReturn(Mono.just(paymentMethod));

		Mono<SaleResponseDto> actualResponse = salesService.updateSale(id, salesInput);

		// then
		StepVerifier.create(actualResponse)
				.expectNextMatches(saleResponseDto -> {
					assertThat("Check final Price", saleResponseDto.getFinalPrice().compareTo(BigDecimal.valueOf(95)) == 0);
					assertThat("Check Points", saleResponseDto.getPoints().compareTo(BigDecimal.valueOf(5)) == 0);
					return true;
				})
				.verifyComplete();
	}
}
