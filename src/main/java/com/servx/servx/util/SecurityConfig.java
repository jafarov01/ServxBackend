package com.servx.servx.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    // No need to inject UserDetailsService, AuthenticationProvider etc. here
    // as your JwtAuthFilter manually sets the SecurityContext

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless API
                .authorizeHttpRequests(auth -> auth
                        // === Existing Rules ===
                        .requestMatchers(HttpMethod.POST, "/api/service-requests").hasAuthority("SERVICE_SEEKER")
                        .requestMatchers(HttpMethod.GET, "/api/service-requests").hasAnyAuthority("SERVICE_SEEKER", "SERVICE_PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/api/service-requests/*").hasAnyAuthority("SERVICE_SEEKER", "SERVICE_PROVIDER") // More specific for ID path
                        .requestMatchers(HttpMethod.PATCH, "/api/service-requests/*/accept").hasAuthority("SERVICE_PROVIDER") // Specific rule for accepting

                        // === Chat API Rules ===
                        .requestMatchers("/api/chats/**").authenticated() // Secure all chat REST endpoints

                        // === Public Endpoints ===
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify-email",
                                "/uploads/**", // Assuming uploads are public or handled differently
                                "/ws/**"       // Allow initial WebSocket connection attempts (handshake)
                        ).permitAll()

                        // === Default Rule ===
                        .anyRequest().authenticated() // All other unspecified requests require authentication
                )
                // Use stateless sessions as JWT is used
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Add your custom JWT filter before the standard username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Define the password encoder bean
        return new BCryptPasswordEncoder();
    }
}