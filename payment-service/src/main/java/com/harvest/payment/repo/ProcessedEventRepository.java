package com.harvest.payment.repo;

import com.harvest.payment.domain.ProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {
    boolean existsByEventId(String eventId);
}