package com.example.demo.config;

import com.example.demo.domain.PersistentEntityCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean
    public PersistentEntityCallback persistentEntityCallback() {
        return new PersistentEntityCallback();
    }

}
