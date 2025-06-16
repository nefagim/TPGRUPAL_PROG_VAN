package com.example.inventorypoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        // For PoC: Create an in-memory user
        // In a real application, this would connect to a database
        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("adminpassword"))
            .roles("ADMIN", "USER")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable CSRF for PoC simplicity with APIs
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll() // Registration/login endpoints
                .antMatchers("/h2-console/**").permitAll() // If using H2 console
                .antMatchers("/api/products/**").hasRole("USER") // Protect product APIs
                .antMatchers("/api/inventory/**").hasRole("USER") // Protect inventory APIs
                .antMatchers("/api/predictor/**").hasRole("USER") // Protect predictor APIs
                .anyRequest().authenticated() // All other requests need authentication
            .and()
            .httpBasic(); // Use HTTP Basic authentication for PoC simplicity

        // For H2 console to work with Spring Security
        http.headers().frameOptions().sameOrigin();
    }
}
