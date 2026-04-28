package com.student.portfolio.config;

import com.student.portfolio.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Disable CSRF for stateless JWT-based API
                .csrf(csrf -> csrf.disable())
                // Enable CORS for frontend communication
                .cors(Customizer.withDefaults())
                // Stateless session management (no server-side sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().migrateSession())
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/api", "/api/", "/api/auth/**", "/api/register", "/api/login", "/api/auth/me", "/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        .anyRequest().authenticated())
                // Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> 
                            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage()))
                        .accessDeniedHandler((request, response, accessDeniedException) -> 
                            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Insufficient permissions")))
                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}