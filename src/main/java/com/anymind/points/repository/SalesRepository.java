/*
 * @author-pankaj.pande
 */

package com.anymind.points.repository;


import com.anymind.points.model.Sale;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

@Transactional
public interface SalesRepository extends ReactiveCrudRepository<Sale, Integer> {
	Mono<Sale> findById(int id);

	Mono<Sale> deleteById(int id);

	Flux<Sale> findByDateTimeBetweenOrderByDateTime(ZonedDateTime from, ZonedDateTime to);
}