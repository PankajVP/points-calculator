/*
 * @author-pankaj.pande
 */

package com.anymind.points.service;

import com.anymind.points.dto.SaleResponseDto;
import com.anymind.points.dto.SalesListDto;
import com.anymind.points.model.DateRangeInput;
import com.anymind.points.model.Sale;
import com.anymind.points.model.SalesInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SalesService {
    Flux<Sale> getAllSales();

    Mono<Sale> getSaleById(Integer id);

    Mono<SaleResponseDto> addSale(SalesInput salesInput);

    Mono<SaleResponseDto> updateSale(Integer id, SalesInput salesInput);

    Flux<SalesListDto> getSaleByRange(DateRangeInput dateRangeInput);

    Mono<Sale> deleteSale(Integer id);
}
