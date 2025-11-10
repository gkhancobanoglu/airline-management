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
import com.cobanoglu.airlinemanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public String register(UserRegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        Role userRole = getRoleByName("USER");
        User user = buildUserFromRequest(request, userRole);
        userRepository.save(user);

        Passenger passenger = buildPassengerFromUser(user);
        passengerRepository.save(passenger);

        return "User registered successfully (Passenger created)";
    }

    @Override
    public String login(String email, String password) {
        authenticateUser(email, password);
        User user = getUserByEmail(email);
        UserDetails userDetails = buildUserDetails(user);
        return jwtService.generateToken(userDetails);
    }


    private void authenticateUser(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    private Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role '" + roleName + "' not found. Please insert it manually first."));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private User buildUserFromRequest(UserRegisterRequest request, Role role) {
        return User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();
    }

    private Passenger buildPassengerFromUser(User user) {
        return Passenger.builder()
                .name(user.getFirstName())
                .surname(user.getLastName())
                .email(user.getEmail())
                .loyaltyPoints(0)
                .build();
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().getName())
                .build();
    }
}
