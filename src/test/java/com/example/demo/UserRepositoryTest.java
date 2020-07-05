package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserRepositoryTest {

    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer();

    @Autowired
    private UserRepository users;

    @DynamicPropertySource
    private static void mongodbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl());
    }

    @BeforeEach
    private void setup() {
        this.users.deleteAll().then().block();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "hantsy", "admin"})
    void testFindByUsername(String name) {
        this.users.save(User.builder().username(name).password("password").roles(List.of("ROLE_USER")).build())
                .then()
                .then(this.users.findByUsername(name))
                .as(StepVerifier::create)
                .consumeNextWith(user -> assertThat(user.getUsername()).isEqualTo(name))
                .verifyComplete();
    }

}
