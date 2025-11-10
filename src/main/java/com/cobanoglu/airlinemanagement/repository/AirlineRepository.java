package com.cobanoglu.airlinemanagement.repository;

import com.cobanoglu.airlinemanagement.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {
    boolean existsByCodeIATA(String codeIATA);
    boolean existsByCodeICAO(String codeICAO);
    boolean existsByCodeIATAAndIdNot(String codeIATA, Long id);
    boolean existsByCodeICAOAndIdNot(String codeICAO, Long id);
}
