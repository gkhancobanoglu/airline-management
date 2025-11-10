package com.cobanoglu.airlinemanagement.repository;

import com.cobanoglu.airlinemanagement.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    boolean existsByEmail(String email);

    Optional<Passenger> findByEmail(String email);
}
