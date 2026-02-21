package com.myApp.ExpenseTracker.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for APIs
                .csrf(csrf -> csrf.disable())

                // Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login",
                                "/api/auth/register" , "/api/auth/income",
                        "/api/reserve" , "/api/reserve/deposit" ,
                                "/api/reserve/withdraw","/api/category",
                                "/api/category/list" , "/api/expense" , "/api/expense/date" , "/api/expense/category","/api/expense/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )

                // Disable default login mechanisms
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }

}
