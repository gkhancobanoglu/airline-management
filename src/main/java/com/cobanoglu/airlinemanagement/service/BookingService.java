package com.cobanoglu.airlinemanagement.service;

import com.cobanoglu.airlinemanagement.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingCreateRequest request);

    void cancelBooking(Long bookingId);

    Page<BookingDTO> listBookings(Pageable pageable);

    BookingDTO getBookingById(Long id);

    void scheduledCancelOldWaitlisted();

    List<PassengerBookingDTO> getBookingHistoryByPassenger(Long passengerId);

    Page<BookingAdminDTO> listAllBookings(Pageable pageable);

    List<PassengerBookingDTO> getCurrentUserBookings();
}
