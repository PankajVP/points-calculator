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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author-pankaj.pande
 */
@Slf4j
@Service
public class SalesServiceImpl implements SalesService {
    private final SalesRepository salesRepository;
    private final MessageSource messageSource;
    private final Locale locale = LocaleContextHolder.getLocale();
    private final PaymentMethodRepository paymentMethodRepository;

    /**
     * Constructor for SalesServiceImpl class that initializes SalesRepository and PaymentMethodRepository.
     *
     * @param salesRepository         - SalesRepository object
     * @param messageSource           - MessageSource Object to return the message string in a given language.
     * @param paymentMethodRepository - PaymentMethodRepository object
     */
    @Autowired
    public SalesServiceImpl(SalesRepository salesRepository, MessageSource messageSource, PaymentMethodRepository paymentMethodRepository) {
        this.salesRepository = salesRepository;
        this.messageSource = messageSource;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    // SALES GET DATA LOGIC-START//

    /**
     * Returns all sales.
     *
     * @return a Flux of Sale objects
     */
    public Flux<Sale> getAllSales() {
        final String errorMessage = messageSource.getMessage("errormessage.get-all-sales", null, locale);
        return salesRepository.findAll()
                .switchIfEmpty(Flux.defer(() -> {
                    log.error(errorMessage);
                    return Flux.error(new CustomException(ErrorType.DataFetchingException, errorMessage));
                }));
    }

    /**
     * Returns sales within the given date range, grouped by hour intervals, along with the total sales and points earned in each interval.
     *
     * @param dateRangeInput the DateRangeInput object representing the date range to filter by
     * @return a Flux of SalesListDto objects
     */
    public Flux<SalesListDto> getSaleByRange(DateRangeInput dateRangeInput) {
        final String errorMessage = messageSource.getMessage("errormessage.get-sales-by-range", new String[]{
                dateRangeInput.getFrom().toString(), dateRangeInput.getTo().toString()}, locale);


        return salesRepository.findByDateTimeBetweenOrderByDateTime(dateRangeInput.getFrom(), dateRangeInput.getTo())
                .switchIfEmpty(Flux.defer(() -> {
                    log.error(errorMessage);
                    return Flux.error(new CustomException(ErrorType.DataFetchingException, errorMessage));
                }))
                .collectList()
                .flatMap(this::groupSalesByInterval)
                .flatMapMany(map -> Flux.fromIterable(map.entrySet()).sort(Comparator.comparing(Map.Entry::getKey)))
                .flatMap(this::getSalesListDto);
    }

    /**
     * Groups sales records by hour intervals.
     *
     * @param salesList a List of Sale objects to group
     * @return a Mono of a Map from ZonedDateTime to List of Sale objects
     */
    Mono<Map<ZonedDateTime, List<Sale>>> groupSalesByInterval(List<Sale> salesList) {
        Map<ZonedDateTime, List<Sale>> salesRecordsByInterval = salesList.stream()
                .collect(Collectors.groupingBy(sale ->
                        sale.getDateTime().truncatedTo(ChronoUnit.HOURS)));
        return Mono.just(salesRecordsByInterval);
    }

    /**
     * Returns a SalesListDto object containing the date and time of the sales interval, as well as the total sales and points earned in that interval.
     *
     * @param salesRecord a Map Entry object representing the sales interval and its corresponding sales records
     * @return a Mono of a SalesListDto object
     */
    Mono<SalesListDto> getSalesListDto(Map.Entry<ZonedDateTime, List<Sale>> salesRecord) {
        List<Sale> saleRecords = salesRecord.getValue();
        return Mono.just(SalesListDto.builder()
                .datetime(salesRecord.getKey())
                .sales(saleRecords.stream().map(o -> o.getFinalPrice()).reduce(BigDecimal.ZERO, BigDecimal::add))
                .points(saleRecords.stream().map(o -> o.getPoints()).reduce(BigDecimal.ZERO, BigDecimal::add))
                .build());

    }

    /**
     * Returns a sale by its ID.
     *
     * @param id the ID of the sale to retrieve
     * @return a Mono of a Sale object
     */
    public Mono<Sale> getSaleById(Integer id) {
        final String errorMessage = messageSource.getMessage("errormessage.get-sales-by-id", new String[]{id.toString()}, locale);
        return salesRepository.findById(id).switchIfEmpty(Mono.defer(() -> {
            log.error(errorMessage);
            return Mono.error(new CustomException(ErrorType.NullValueInNonNullableField, errorMessage));
        }));
    }

    // SALES ADD/UPDATE/DELETE DATA LOGIC-START//

    /**
     * Adds a new sale with the given sales input and returns a Mono of SaleResponseDto.
     *
     * @param salesInput the SalesInput object to be added
     * @return a Mono of SaleResponseDto
     */
    public Mono<SaleResponseDto> addSale(SalesInput salesInput) {
        return verifyPaymentMethod(salesInput)
                .flatMap(this::verifySaleData)
                .flatMap(this::saveSale)
                .flatMap(this::getPriceDto);
    }

    /**
     * Returns a Mono of SaleResponseDto for the given Sale object.
     *
     * @param sale the Sale object to be used to create a SaleResponseDto
     * @return a Mono of SaleResponseDto
     */
    Mono<SaleResponseDto> getPriceDto(Sale sale) {
        return Mono.just(SaleResponseDto.builder()
                .finalPrice(sale.getFinalPrice().setScale(BigDecimal.ROUND_HALF_UP))
                .points(sale.getPoints())
                .build());
    }

    /**
     * Saves the sale data and returns a Mono of Sale.
     *
     * @param requestData the RequestData object containing the sales input and payment method
     * @return a Mono of Sale
     */
    public Mono<Sale> saveSale(RequestData requestData) {
        final String errorMessage = messageSource.getMessage("errormessage.save-sale", new String[]{requestData.getSalesInput().toString()}, locale);
        SalesInput salesInput = requestData.getSalesInput();
        PaymentMethod paymentMethod = requestData.getPaymentMethod();

        return salesRepository.save(
                Sale.builder()
                        .id(requestData.getSalesId() != null ? requestData.getSalesId() : null)
                        .finalPrice(salesInput.getPrice().multiply(salesInput.getPriceModifier()))
                        .points(salesInput.getPrice().multiply(paymentMethod.getPointsModifier()))
                        .paymentMethodId(paymentMethod.getId())
                        .dateTime(salesInput.getDatetime())
                        .build()
        ).switchIfEmpty(Mono.defer(() -> {
            log.error(errorMessage);
            return Mono.error(new CustomException(ErrorType.ExecutionAborted, errorMessage));
        }));
    }

    /**
     * Verifies the payment method and returns a Mono of RequestData.
     *
     * @param salesInput the SalesInput object to be verified
     * @return a Mono of RequestData
     */
    public Mono<RequestData> verifyPaymentMethod(SalesInput salesInput) {
        final String errorMessage = messageSource.getMessage("errormessage.verify-payment-method", new String[]{salesInput.toString()}, locale);
        return paymentMethodRepository.findByName(salesInput.getPaymentMethod())
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(errorMessage);
                    return Mono.error(new CustomException(ErrorType.InvalidSyntax, errorMessage));
                }))
                .flatMap(paymentMethod -> Mono.just(RequestData.builder()
                        .paymentMethod(paymentMethod)
                        .salesInput(salesInput)
                        .build())
                ).flatMap(this::verifySaleData);
    }

    /**
     * Verifies the sales data and returns a Mono of RequestData.
     *
     * @param requestData the RequestData object to be verified
     * @return a Mono of RequestData
     */
    Mono<RequestData> verifySaleData(RequestData requestData) {
        final String errorMessage = messageSource.getMessage("errormessage.verify-sale-data", new String[]{requestData.getSalesInput().getPriceModifier().toString()}, locale);
        if (!(requestData.getSalesInput().getPriceModifier().compareTo(requestData.getPaymentMethod().getPriceModifierFrom()) >= 0
                && requestData.getSalesInput().getPriceModifier().compareTo(requestData.getPaymentMethod().getPriceModifierTo()) <= 0))
            return Mono.error(new CustomException(ErrorType.InvalidSyntax, errorMessage));
        else
            return Mono.just(requestData);
    }

    /**
     * Updates an existing sale with the given ID and sales input and returns a Mono of SaleResponseDto.
     *
     * @param id         the ID of the sale to be updated
     * @param salesInput the SalesInput object to be updated
     * @return a Mono of SaleResponseDto
     */
    public Mono<SaleResponseDto> updateSale(Integer id, SalesInput salesInput) {
        final String errorMessage = "Unable to update Sale with id" + id + "input:" + salesInput;
        return verifyPaymentMethod(salesInput)
                .flatMap(this::verifySaleData)
                .flatMap(requestData -> {
                    requestData.setSalesId(id);
                    return Mono.just(requestData);
                })
                .flatMap(this::saveSale)
                .flatMap(this::getPriceDto);
    }

    /**
     * Deletes a sale with the given ID and returns a Mono of Sale.
     *
     * @param id the ID of the sale to be deleted
     * @return a Mono of Sale
     */
    public Mono<Sale> deleteSale(Integer id) {
        return salesRepository.findById(id).map(sale -> {
            salesRepository.deleteById(id).subscribe();
            return sale;
        });
    }

}
