package com.ruleengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig — opens all /api/rules/** endpoints for Postman testing.
 *
 * In production:
 *  - Use the X-Zapier-Secret header validation in the controller
 *  - Or add IP allowlisting for Zapier's IP ranges
 *  - Or add proper JWT/OAuth2 authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // Allow H2 console frames
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(auth -> auth
                // All rule engine endpoints — open for Postman testing
                .requestMatchers("/api/rules/**").permitAll()
                // H2 console — open for development
                .requestMatchers("/h2-console/**").permitAll()
                // Actuator health endpoint
                .requestMatchers("/actuator/health").permitAll()
                // Everything else requires auth
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
