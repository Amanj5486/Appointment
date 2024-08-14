package com.example.appointment.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {


    private final TokenConfiguaration tokenConfiguaration;

    public SecurityConfiguration(TokenConfiguaration tokenConfiguaration) {
        this.tokenConfiguaration = tokenConfiguaration;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }



    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService detailsService){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider() ;
        daoAuthenticationProvider.setUserDetailsService(detailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }


    @Bean
    public SecurityFilterChain filterChainDoctor(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/appointment/**").permitAll();
                    auth.anyRequest().authenticated();
                });
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .permitAll())
//        ;

        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(tokenConfiguaration.jwtAuthenticationConverterPatient());
        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        return http.build();
    }


}
