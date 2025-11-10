package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.*;
import com.cobanoglu.airlinemanagement.entity.*;
import com.cobanoglu.airlinemanagement.exception.*;
import com.cobanoglu.airlinemanagement.mapper.BookingMapper;
import com.cobanoglu.airlinemanagement.repository.*;
import com.cobanoglu.airlinemanagement.service.PassengerService;
import com.cobanoglu.airlinemanagement.util.DateUtils;
import com.cobanoglu.airlinemanagement.util.PriceCalculator;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private PassengerRepository passengerRepository;
    @Mock private UserRepository userRepository;
    @Mock private PassengerService passengerService;
    @Mock private BookingMapper bookingMapper;
    @Mock private PriceCalculator priceCalculator;
    @Mock private DateUtils dateUtils;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private AutoCloseable closeable;

    private Flight flight;
    private Passenger passenger;
    private User user;
    private Booking booking;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("TK100");
        flight.setBasePrice(BigDecimal.valueOf(1000));
        flight.setCapacity(100);
        flight.setBookedSeats(50);
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));

        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setEmail("test@example.com");
        passenger.setLoyaltyPoints(1200);

        user = new User();
        user.setEmail("test@example.com");
        Role r = new Role();
        r.setName("USER");
        user.setRole(r);

        booking = new Booking();
        booking.setId(10L);
        booking.setFlight(flight);
        booking.setPassenger(passenger);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPrice(BigDecimal.valueOf(900));
        booking.setCreateDate(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private void mockAuthority(String roleName) {
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn(roleName);
        when(authentication.getAuthorities()).thenReturn((Collection) List.of(authority));
    }

    @Test
    void createBooking_successfulConfirmed() {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setFlightId(1L);
        req.setSeatNumber("12A");

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passengerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(passenger));
        when(dateUtils.isFlightExpired(any())).thenReturn(false);
        when(bookingRepository.existsByFlight_IdAndPassenger_Id(anyLong(), anyLong())).thenReturn(false);
        when(bookingRepository.existsByFlight_IdAndSeatNumberIgnoreCase(anyLong(), anyString())).thenReturn(false);
        when(priceCalculator.calculatePrice(any(), anyDouble())).thenReturn(BigDecimal.valueOf(1000));

        BookingResponse res = bookingService.createBooking(req);

        assertNotNull(res);
        assertEquals(BookingStatus.CONFIRMED, res.getStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(passengerService).updateLoyaltyPoints(eq(1L), anyInt());
        verify(flightRepository).save(flight);
    }

    @Test
    void createBooking_shouldThrowOverbookingException() {
        flight.setBookedSeats(111); // exceeds 110%
        BookingCreateRequest req = new BookingCreateRequest();
        req.setFlightId(1L);
        req.setSeatNumber("12A");

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passengerRepository.findByEmail(any())).thenReturn(Optional.of(passenger));
        when(dateUtils.isFlightExpired(any())).thenReturn(false);
        when(priceCalculator.calculatePrice(any(), anyDouble())).thenReturn(BigDecimal.valueOf(1000));
        when(bookingRepository.existsByFlight_IdAndPassenger_Id(any(), any())).thenReturn(false);
        when(bookingRepository.existsByFlight_IdAndSeatNumberIgnoreCase(any(), any())).thenReturn(false);

        assertThrows(OverbookingException.class, () -> bookingService.createBooking(req));
    }

    @Test
    void cancelBooking_shouldCancelAndRefund() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        mockAuthority("ROLE_ADMIN");

        bookingService.cancelBooking(10L);

        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
        verify(bookingRepository, atLeastOnce()).save(booking);
        verify(passengerService, atLeastOnce()).updateLoyaltyPoints(anyLong(), anyInt());
    }

    @Test
    void cancelBooking_shouldThrowIfAlreadyCancelled() {
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        mockAuthority("ROLE_ADMIN");

        assertThrows(BadRequestException.class, () -> bookingService.cancelBooking(10L));
    }

    @Test
    void scheduledCancelOldWaitlisted_shouldCancelOld() {
        Booking oldWait = new Booking();
        oldWait.setId(5L);
        oldWait.setBookingStatus(BookingStatus.WAITLISTED);
        oldWait.setFlight(flight);

        when(bookingRepository.findAllByBookingStatusAndFlight_DepartureTimeBefore(any(), any()))
                .thenReturn(List.of(oldWait));

        bookingService.scheduledCancelOldWaitlisted();

        verify(bookingRepository).save(oldWait);
        assertEquals(BookingStatus.CANCELLED, oldWait.getBookingStatus());
    }

    @Test
    void getBookingById_userAccessDenied() {
        Booking otherBooking = new Booking();
        Passenger otherPassenger = new Passenger();
        otherPassenger.setEmail("other@example.com");
        otherBooking.setPassenger(otherPassenger);

        mockAuthority("ROLE_USER");
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(otherBooking));

        assertThrows(AccessDeniedException.class, () -> bookingService.getBookingById(1L));
    }

    @Test
    void getBookingById_adminCanView() {
        mockAuthority("ROLE_ADMIN");
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(any())).thenReturn(new BookingDTO());

        BookingDTO dto = bookingService.getBookingById(10L);

        assertNotNull(dto);
        verify(bookingMapper).toDto(booking);
    }
}
