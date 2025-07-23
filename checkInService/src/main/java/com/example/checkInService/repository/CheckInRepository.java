package com.example.checkInService.repository;

import com.example.checkInService.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findByBookingIdAndPassengerId(Long bookingId, Long passengerId);
}
