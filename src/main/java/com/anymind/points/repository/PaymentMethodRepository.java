package com.anymind.points.repository;


import com.anymind.points.model.PaymentMethod;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


@Transactional
public interface PaymentMethodRepository extends ReactiveCrudRepository<PaymentMethod, Integer> {
	Mono<PaymentMethod> findById(int id);

	Mono<PaymentMethod> deleteById(int id);

	Mono<PaymentMethod> findByName(String name);
}