package com.university.UniversityPortal.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF so POST/DELETE work from your HTTP file
                .csrf(csrf -> csrf.disable())

                // 2. Authorize all requests
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )

                // 3. Enable Basic Auth (for that user/pass header)
                .httpBasic(withDefaults());

        return http.build();
    }
}