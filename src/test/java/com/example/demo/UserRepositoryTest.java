package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
@Slf4j
class UserRepositoryTest {

    @Container
    @ServiceConnection
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6");

    @Autowired
    private UserRepository users;

    @BeforeEach
    void setup() {
        log.debug("setup UserRepositoryTest...");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "hantsy", "admin"})
    void testFindByUsername(String name) {
        this.users.save(User.builder().username(name).password("password").roles(List.of("ROLE_USER")).build())
                .flatMap(__ -> this.users.findByUsername(name))
                .as(StepVerifier::create)
                .consumeNextWith(user -> assertThat(user.getUsername()).isEqualTo(name))
                .verifyComplete();
    }

}
