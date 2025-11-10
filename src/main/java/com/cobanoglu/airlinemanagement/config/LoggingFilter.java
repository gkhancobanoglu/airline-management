package com.cobanoglu.airlinemanagement.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        log.info("➡️ Request: {} {}", req.getMethod(), req.getRequestURI());

        chain.doFilter(request, response);

        HttpServletResponse res = (HttpServletResponse) response;
        log.info("⬅️ Response: {} {}", res.getStatus(), req.getRequestURI());
    }
}
