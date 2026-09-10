package com.harvest.payment.repo;

import com.harvest.payment.domain.Payment;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByOrderNumber(String orderNumber);
}