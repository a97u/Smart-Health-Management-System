package com.hospital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Configuring security filter chain for REST API");
        
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/images/**", "/favicon.ico").permitAll() // Keep access to visualization resources
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/doctors/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/api/nurses/**").hasAnyRole("NURSE", "ADMIN")
                .requestMatchers("/api/patients/**").hasAnyRole("PATIENT", "DOCTOR", "NURSE", "ADMIN")
                .requestMatchers("/api/records/**").hasAnyRole("DOCTOR", "NURSE", "ADMIN", "PATIENT")
                .requestMatchers("/api/health-metrics/**").hasAnyRole("DOCTOR", "NURSE", "ADMIN", "PATIENT")
                .requestMatchers("/health-metrics/chart/**").permitAll() // Allow access to chart data
                .anyRequest().authenticated()
            )
            .httpBasic() // Use HTTP Basic Authentication for API
            .and()
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Don't create sessions for REST API
            )
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    logger.debug("Authentication entry point triggered: {}", authException.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
            )
            .formLogin(form -> form.disable()) // Disable form login for REST API
            .logout(logout -> logout
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Logout successful\"}");
                })
            );
        
        logger.debug("Security filter chain configured successfully");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}