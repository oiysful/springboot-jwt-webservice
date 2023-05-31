package com.ian.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing  // created_at, updated_at 자동 생성
@SpringBootApplication
public class SpringBootJwtApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootJwtApplication.class, args);
    }
}