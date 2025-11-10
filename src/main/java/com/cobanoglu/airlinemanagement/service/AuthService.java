package com.cobanoglu.airlinemanagement.service;

import com.cobanoglu.airlinemanagement.dto.UserRegisterRequest;

public interface AuthService {

    String register(UserRegisterRequest userRegisterRequest);
    String login(String email, String password);
}
