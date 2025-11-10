package com.cobanoglu.airlinemanagement.repository;

import com.cobanoglu.airlinemanagement.entity.Booking;
import com.cobanoglu.airlinemanagement.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByFlight_Id(Long flightId);

    boolean existsByFlight_Id(Long flightId);

    boolean existsByFlight_IdAndPassenger_Id(Long flightId, Long passengerId);

    boolean existsByFlight_IdAndSeatNumberIgnoreCase(Long flightId, String seatNumber);

    boolean existsByFlight_Airline_Id(Long airlineId);

    List<Booking> findByPassenger_Id(Long passengerId);

    List<Booking> findAllByBookingStatusAndFlight_DepartureTimeBefore(
            BookingStatus bookingStatus,
            LocalDateTime beforeTime
    );
}
