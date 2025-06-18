package com.empresa.demostockapp.security;

import com.empresa.demostockapp.security.jwt.AuthEntryPointJwt;
import com.empresa.demostockapp.security.jwt.AuthTokenFilter;
import com.empresa.demostockapp.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For .csrf(AbstractHttpConfigurer::disable)
import org.springframework.security.config.http.SessionCreationPolicy;
import static org.springframework.security.config.Customizer.withDefaults; // Added for cors(withDefaults())
import org.springframework.web.cors.CorsConfiguration; // Added
import org.springframework.web.cors.CorsConfigurationSource; // Added
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Added
import java.util.Arrays; // Added
import java.util.List; // Added
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity //  to enable method level security like @PreAuthorize
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults()) // Enable CORS using the bean by default
                .csrf(AbstractHttpConfigurer::disable) // Updated for clarity, same effect
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/products/**").authenticated() // Secure product APIs
                                .requestMatchers("/api/predictions/**").hasAnyRole("MANAGER", "ADMIN") // Secure prediction APIs
                                .requestMatchers("/api/stock/**").hasAnyRole("MANAGER", "ADMIN") // Secure stock APIs
                                .requestMatchers("/api/salesorders/**").hasAnyRole("MANAGER", "ADMIN") // Secure sales order APIs
                                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                                .anyRequest().authenticated()
                );

        // Required for H2 console to work with Spring Security
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from React's default development server port
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin", // Important for CORS
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        // How long the results of a pre-flight request can be cached
        configuration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply this configuration to all paths
        return source;
    }
}
