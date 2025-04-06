package com.hospital.config;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom authentication success handler for REST API
 * Returns JSON response with authentication details instead of redirecting
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        logger.info("Authentication success handler triggered for user: {}", authentication.getName());
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        logger.info("User authorities: {}", authorities);
        
        String username = authentication.getName();
        logger.info("Authenticated user: {}", username);
        
        // Determine user role
        String role = "";
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            logger.info("Processing authority: {}", auth);
            
            if (auth.equals("ROLE_ADMIN")) {
                role = "ADMIN";
                break;
            } else if (auth.equals("ROLE_DOCTOR")) {
                role = "DOCTOR";
                break;
            } else if (auth.equals("ROLE_NURSE")) {
                role = "NURSE";
                break;
            } else if (auth.equals("ROLE_PATIENT")) {
                role = "PATIENT";
                break;
            }
        }
        
        if (role.isEmpty()) {
            logger.warn("No recognized role found for user {}. Authorities: {}", username, authorities);
            role = "UNKNOWN";
        }
        
        // Create response JSON
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Authentication successful");
        responseData.put("username", username);
        responseData.put("role", role);
        
        // Set response headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        
        // Write JSON response
        objectMapper.writeValue(response.getWriter(), responseData);
        
        logger.info("Authentication success response sent for user: {}", username);
    }
}