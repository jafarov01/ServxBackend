package com.servx.servx.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register", // Add this line
                                "/api/auth/login",
                                "/api/services/categories",
                                "/api/services/areas/{categoryId}",
                                "/api/auth/verify-email"
                        ).permitAll()
                        .anyRequest().authenticated() // All other endpoints require authentication
                )
                .httpBasic(Customizer.withDefaults()) // Use HTTP Basic authentication (optional, can be replaced with JWT later)
                .formLogin(form -> form.defaultSuccessUrl("/").permitAll()) // Enable form-based login
                .logout(LogoutConfigurer::permitAll) // Enable logout
                .csrf(AbstractHttpConfigurer::disable); // Disable CSRF for testing (enable in production with proper tokens)

        return http.build();
    }
}
