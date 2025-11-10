package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.*;
import com.cobanoglu.airlinemanagement.entity.*;
import com.cobanoglu.airlinemanagement.exception.*;
import com.cobanoglu.airlinemanagement.mapper.BookingMapper;
import com.cobanoglu.airlinemanagement.mapper.PassengerBookingMapper;
import com.cobanoglu.airlinemanagement.repository.*;
import com.cobanoglu.airlinemanagement.service.*;
import com.cobanoglu.airlinemanagement.util.DateUtils;
import com.cobanoglu.airlinemanagement.util.PriceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final PassengerService passengerService;
    private final BookingMapper bookingMapper;
    private final PassengerBookingMapper passengerBookingMapper;
    private final PriceCalculator priceCalculator;
    private final DateUtils dateUtils;

    @Override
    public BookingResponse createBooking(BookingCreateRequest request) {
        Flight flight = getFlightOrThrow(request.getFlightId());
        String email = getUserEmail();
        User user = getUserOrThrow(email);
        Passenger passenger = resolvePassengerForBooking(request, user, email);

        validateBookingRules(flight, passenger, request);

        double occupancyRate = (double) flight.getBookedSeats() / flight.getCapacity();
        BigDecimal dynamicPrice = calculateDynamicPrice(flight, passenger, occupancyRate);

        int overbookingLimit = (int) Math.round(flight.getCapacity() * 1.10);
        if (flight.getBookedSeats() >= overbookingLimit) {
            throw new OverbookingException("Overbooking limit reached for flight " + flight.getFlightNumber());
        }

        BookingStatus status = flight.getBookedSeats() < flight.getCapacity()
                ? BookingStatus.CONFIRMED : BookingStatus.WAITLISTED;

        Booking booking = Booking.builder()
                .flight(flight)
                .passenger(passenger)
                .seatNumber(request.getSeatNumber().trim().toUpperCase())
                .bookingStatus(status)
                .price(dynamicPrice)
                .createDate(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        if (status == BookingStatus.CONFIRMED) handleConfirmedBooking(flight, passenger, dynamicPrice);

        sendBookingEmailAsync(passenger.getEmail(), booking.getBookingStatus());

        return new BookingResponse(
                booking.getId(),
                booking.getBookingStatus(),
                dynamicPrice,
                "Booking created successfully with status: " + booking.getBookingStatus()
        );
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Authentication auth = getAuth();
        String username = auth.getName();
        boolean isUser = hasRole(auth, "ROLE_USER");

        Booking booking = getBookingOrThrow(bookingId);

        if (isUser && !booking.getPassenger().getEmail().equals(username)) {
            throw new AccessDeniedException("You are not authorized to cancel this booking");
        }

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking already cancelled");
        }

        BookingStatus originalStatus = booking.getBookingStatus();
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        if (originalStatus == BookingStatus.CONFIRMED) handleConfirmedCancellation(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> listBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        Authentication auth = getAuth();
        String username = auth.getName();
        boolean isUser = hasRole(auth, "ROLE_USER");

        Booking booking = getBookingOrThrow(id);

        if (isUser && !booking.getPassenger().getEmail().equals(username)) {
            throw new AccessDeniedException("You are not authorized to view this booking");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledCancelOldWaitlisted() {
        bookingRepository
                .findAllByBookingStatusAndFlight_DepartureTimeBefore(
                        BookingStatus.WAITLISTED, LocalDateTime.now())
                .forEach(b -> {
                    b.setBookingStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(b);
                    log.info("Auto-cancelled WAITLISTED booking {} (flight departed)", b.getId());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerBookingDTO> getBookingHistoryByPassenger(Long passengerId) {
        Authentication auth = getAuth();
        String username = auth.getName();
        boolean isUser = hasRole(auth, "ROLE_USER");

        if (isUser) {
            Passenger me = getPassengerOrThrow(username);
            if (!me.getId().equals(passengerId)) {
                throw new AccessDeniedException("You are not authorized to view another passenger's bookings");
            }
        }

        List<Booking> bookings = bookingRepository.findByPassenger_Id(passengerId)
                .stream()
                .sorted(Comparator.comparing(b -> b.getFlight().getDepartureTime()))
                .toList();

        return passengerBookingMapper.toDtoList(bookings);
    }

    @Override
    public Page<BookingAdminDTO> listAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::mapToAdminDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerBookingDTO> getCurrentUserBookings() {
        String username = getUserEmail();
        Passenger passenger = getPassengerOrThrow(username);

        List<Booking> bookings = bookingRepository.findByPassenger_Id(passenger.getId())
                .stream()
                .sorted(Comparator.comparing(b -> b.getFlight().getDepartureTime()))
                .toList();

        return passengerBookingMapper.toDtoList(bookings);
    }

    @Async
    protected void sendBookingEmailAsync(String email, BookingStatus status) {
        log.info("Sending confirmation email to {} for booking status: {}", email, status);
    }

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getUserEmail() {
        return getAuth().getName();
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    private Flight getFlightOrThrow(Long flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new NotFoundException("Flight not found with id: " + flightId));
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private Passenger getPassengerOrThrow(String email) {
        return passengerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Passenger not found for user: " + email));
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
    }

    private Passenger resolvePassengerForBooking(BookingCreateRequest req, User user, String email) {
        boolean isAdmin = user.getRole().getName().equalsIgnoreCase("ADMIN");
        if (isAdmin && req.getPassengerId() != null) {
            return passengerRepository.findById(req.getPassengerId())
                    .orElseThrow(() -> new NotFoundException("Passenger not found with id: " + req.getPassengerId()));
        }
        return getPassengerOrThrow(email);
    }

    private void validateBookingRules(Flight flight, Passenger passenger, BookingCreateRequest req) {
        if (dateUtils.isFlightExpired(flight.getDepartureTime())) {
            throw new BadRequestException("Cannot book a flight that has already departed");
        }
        if (bookingRepository.existsByFlight_IdAndPassenger_Id(flight.getId(), passenger.getId())) {
            throw new BadRequestException("Passenger already has a booking for this flight");
        }
        String seat = req.getSeatNumber().trim().toUpperCase();
        if (bookingRepository.existsByFlight_IdAndSeatNumberIgnoreCase(flight.getId(), seat)) {
            throw new BadRequestException("This seat is already taken");
        }
    }

    private BigDecimal calculateDynamicPrice(Flight flight, Passenger passenger, double occupancyRate) {
        BigDecimal price = priceCalculator.calculatePrice(flight.getBasePrice(), occupancyRate);
        return passenger.getLoyaltyPoints() > 1000
                ? price.multiply(BigDecimal.valueOf(0.9))
                : price;
    }

    private void handleConfirmedBooking(Flight flight, Passenger passenger, BigDecimal price) {
        flight.setBookedSeats(flight.getBookedSeats() + 1);
        flightRepository.save(flight);
        int loyaltyGain = price.multiply(BigDecimal.valueOf(0.10)).intValue();
        passengerService.updateLoyaltyPoints(passenger.getId(), loyaltyGain);
    }

    private void handleConfirmedCancellation(Booking booking) {
        BigDecimal refund = booking.getPrice().multiply(BigDecimal.valueOf(0.8));
        log.info("Refunded 80% of booking {}: {}", booking.getId(), refund);

        int lostPoints = booking.getPrice().multiply(BigDecimal.valueOf(0.10)).intValue();
        passengerService.updateLoyaltyPoints(booking.getPassenger().getId(), -lostPoints);

        Flight flight = booking.getFlight();
        if (flight.getBookedSeats() > 0) {
            flight.setBookedSeats(flight.getBookedSeats() - 1);
            flightRepository.save(flight);
        }

        promoteWaitlistedToConfirmed(flight);
    }

    private BookingAdminDTO mapToAdminDTO(Booking booking) {
        Flight f = booking.getFlight();
        Passenger p = booking.getPassenger();
        return BookingAdminDTO.builder()
                .id(booking.getId())
                .flightNumber(f != null ? f.getFlightNumber() : "-")
                .origin(f != null ? f.getOrigin() : "-")
                .destination(f != null ? f.getDestination() : "-")
                .passengerName(p != null ? p.getName() + " " + p.getSurname() : "-")
                .seatNumber(booking.getSeatNumber())
                .bookingStatus(booking.getBookingStatus().name())
                .price(booking.getPrice())
                .build();
    }

    private void promoteWaitlistedToConfirmed(Flight flight) {
        List<Booking> waitlisted = bookingRepository.findByFlight_Id(flight.getId()).stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.WAITLISTED)
                .sorted(Comparator.comparing(Booking::getCreateDate))
                .toList();

        int overbookingLimit = (int) Math.round(flight.getCapacity() * 1.10);

        if (!waitlisted.isEmpty() && flight.getBookedSeats() < overbookingLimit) {
            Booking next = waitlisted.get(0);
            next.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(next);

            flight.setBookedSeats(flight.getBookedSeats() + 1);
            flightRepository.save(flight);

            int loyaltyGain = next.getPrice().multiply(BigDecimal.valueOf(0.10)).intValue();
            passengerService.updateLoyaltyPoints(next.getPassenger().getId(), loyaltyGain);

            log.info("Promoted WAITLISTED booking {} to CONFIRMED", next.getId());
        }
    }
}
