package com.servx.servx.config;

import com.servx.servx.enums.Role;
import com.servx.servx.util.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Good to have
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC Endpoints ---
                        // Paths permitted for ANY HTTP method
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "api/auth/reset-password",
                                "/api/auth/verify-email",
                                "/uploads/**",
                                "/ws/**",
                                "reset-password.html"
                        ).permitAll()
                        // GET requests permitted for anyone
                        .requestMatchers(HttpMethod.GET, "/api/reviews/service/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/service-profiles/**").permitAll()

                        // --- ROLE-SPECIFIC Endpoints ---
                        // Service Requests
                        .requestMatchers(HttpMethod.POST, "/api/service-requests").hasAuthority(Role.SERVICE_SEEKER.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/service-requests/*/accept").hasAuthority(Role.SERVICE_PROVIDER.name())
                        .requestMatchers(HttpMethod.POST, "/api/service-requests/*/confirm-booking/**", "/api/service-requests/*/reject-booking").hasAuthority(Role.SERVICE_SEEKER.name())
                        // Booking Completion
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/provider-complete").hasAuthority(Role.SERVICE_PROVIDER.name())
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/seeker-confirm").hasAuthority(Role.SERVICE_SEEKER.name())
                        // Review Submission
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasAuthority(Role.SERVICE_SEEKER.name())
                        // User Upgrade
                        .requestMatchers(HttpMethod.POST, "/api/user/me/upgrade-to-provider").hasAuthority(Role.SERVICE_SEEKER.name())

                        // --- AUTHENTICATED (Any Role) Endpoints ---
                        .requestMatchers(HttpMethod.GET, "/api/service-requests", "/api/service-requests/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings", "/api/bookings/by-date").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/cancel").authenticated()
                        .requestMatchers("/api/chats/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/user/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/user/me", "/api/user/me/photo").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/user/me/photo").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()


                        // --- Default Rule ---
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}