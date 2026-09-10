package com.harvest.gateway.config;

import com.harvest.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtBeanConfig {
    @Bean
    JwtService jwtService(@Value("${app.jwt.secret}") String secret) {
        return new JwtService(secret);
    }
}