package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.UserRegisterRequest;
import com.cobanoglu.airlinemanagement.entity.Passenger;
import com.cobanoglu.airlinemanagement.entity.Role;
import com.cobanoglu.airlinemanagement.entity.User;
import com.cobanoglu.airlinemanagement.exception.BadRequestException;
import com.cobanoglu.airlinemanagement.exception.NotFoundException;
import com.cobanoglu.airlinemanagement.repository.PassengerRepository;
import com.cobanoglu.airlinemanagement.repository.RoleRepository;
import com.cobanoglu.airlinemanagement.repository.UserRepository;
import com.cobanoglu.airlinemanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegisterRequest request;
    private Role role;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new UserRegisterRequest();
        request.setFirstName("Gokhan");
        request.setLastName("Cobanoglu");
        request.setEmail("gokhan@example.com");
        request.setPassword("password");

        role = new Role();
        role.setId(1L);
        role.setName("USER");

        user = new User();
        user.setId(1L);
        user.setFirstName("Gokhan");
        user.setLastName("Cobanoglu");
        user.setEmail("gokhan@example.com");
        user.setPassword("encodedPass");
        user.setRole(role);
        user.setActive(true);
    }

    @Test
    void register_success_createsUserAndPassenger() {
        when(userRepository.findByEmail("gokhan@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        String result = authService.register(request);

        assertEquals("User registered successfully (Passenger created)", result);
        verify(userRepository).save(any(User.class));
        verify(passengerRepository).save(any(Passenger.class));
    }

    @Test
    void register_emailAlreadyExists_throwsBadRequest() {
        when(userRepository.findByEmail("gokhan@example.com")).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_roleNotFound_throwsNotFound() {
        when(userRepository.findByEmail("gokhan@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> authService.register(request));
    }

    @Test
    void login_success_returnsJwtToken() {
        when(userRepository.findByEmail("gokhan@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mocked.jwt.token");

        String result = authService.login("gokhan@example.com", "password");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("mocked.jwt.token", result);
    }

    @Test
    void login_userNotFound_throwsNotFound() {
        when(userRepository.findByEmail("gokhan@example.com")).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        assertThrows(NotFoundException.class, () -> authService.login("gokhan@example.com", "password"));
    }


    @Test
    void login_authenticationFails_stillThrowsWhenUserMissing() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> authService.login("notfound@example.com", "pass"));
    }


    @Test
    void register_trimsAndLowercasesInput() {
        request.setFirstName(" GOKHAN ");
        request.setLastName(" COBANOGLU ");
        request.setEmail("  GOKHAN@EXAMPLE.COM  ");
        when(userRepository.findByEmail("gokhan@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("gokhan@example.com", saved.getEmail());
        assertEquals("GOKHAN", saved.getFirstName());
        assertEquals("COBANOGLU", saved.getLastName());
    }
}
