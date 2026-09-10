package com.harvest.inventory.repo;

import com.harvest.inventory.domain.Reservation;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
    Optional<Reservation> findByOrderId(String orderId);
}