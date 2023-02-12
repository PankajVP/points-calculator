/*
 * @author-pankaj.pande
 */

package com.anymind.points.controller;

import com.anymind.points.dto.SaleResponseDto;
import com.anymind.points.dto.SalesListDto;
import com.anymind.points.model.DateRangeInput;
import com.anymind.points.model.Sale;
import com.anymind.points.model.SalesInput;
import com.anymind.points.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.Locale;
import java.util.logging.Level;

@Slf4j
@Controller
public class SalesSystemController {
    private final SalesService salesService;


    @Autowired
    public SalesSystemController(SalesService salesService) {
        this.salesService = salesService;
    }

    @QueryMapping("getAllSales")
    Flux<Sale> getAllSales() {
        log.debug("Get all sales using 'getAllSales' query");
        return processWithLog(salesService.getAllSales());
    }

    @QueryMapping("getSaleById")
    Mono<Sale> getSaleById(@Argument Integer id) {
        log.debug("Get Sale by id using 'getSaleById' query");
        return processWithLog(salesService.getSaleById(id));
    }

    /**
     * ASSIGNMENT METHOD 2:
     * Returns a Flux of SalesListDto objects for a given date range using the 'getSaleByName' query argument.
     *
     * @param dateRangeInput The input object specifying the date range to query. Must not be null.
     * @return A Flux of SalesListDto objects for the given date range. May be empty.
     * @throws NullPointerException if the dateRangeInput argument is null.
     */
    @QueryMapping("getSalesByRange")
    Flux<SalesListDto> getSaleByRange(@Argument DateRangeInput dateRangeInput,Locale locale) {
        log.debug("Get Sale by range using 'getSaleByName' query argument dateRangeInput::" + dateRangeInput);
        return processWithLog(salesService.getSaleByRange(dateRangeInput));
    }


    /**
     * ASSIGNMENT METHOD 1:
     * This method receives a SalesInput object and saves it to the database using the SalesService class. It then returns a Mono object containing a SaleResponseDto object with information about the saved sale.
     *
     * @param salesInput An object representing the sale to be saved. Must not be null.
     * @return A Mono object containing a SaleResponseDto with information about the saved sale. May be empty.
     * @throws NullPointerException if the salesInput parameter is null.
     */
    @MutationMapping("addSale")
    Mono<SaleResponseDto> addSale(@Argument("salesInput") SalesInput salesInput) {
        log.debug("Add Sale using 'addSale' mutation::" + salesInput);
        return salesService.addSale(salesInput);
    }

    @MutationMapping("updateSale")
    Mono<SaleResponseDto> updateSale(@Argument Integer id, @Argument("salesInput") SalesInput salesInput) {
        log.debug("Updating Sale using 'updateSale' mutation for id" + id + " salesInput::" + salesInput);
        return processWithLog(salesService.updateSale(id, salesInput));
    }

    @MutationMapping("deleteSaleById")
    Mono<Sale> deleteSaleById(@Argument Integer id) {
        log.debug("Delete Sale using 'deleteSaleById' mutation for id::" + id);
        return processWithLog(salesService.deleteSale(id));
    }

    private <T> Mono<T> processWithLog(Mono<T> monoToLog) {
        return monoToLog.log("SalesGraphQLController.", Level.INFO, SignalType.ON_NEXT, SignalType.ON_COMPLETE);
    }

    private <T> Flux<T> processWithLog(Flux<T> fluxToLog) {
        return fluxToLog.log("SalesGraphQLController.", Level.INFO, SignalType.ON_NEXT, SignalType.ON_COMPLETE);
    }
}
