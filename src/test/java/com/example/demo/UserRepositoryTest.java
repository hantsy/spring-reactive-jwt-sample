package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.List;

@DataMongoTest
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UserRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer();

    @Autowired
    UserRepository users;

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl());
    }

    @BeforeEach
    public void setup() {
        this.users.deleteAll().then()
                .then(
                        this.users.save(User.builder().username("test").password("password").roles(List.of("ROLE_USER")).build())
                )
                .block();
    }

    @Test
    public void testFindByUsername() {
        this.users.findByUsername("test")
                .as(StepVerifier::create)
                //.consumeNextWith(user-> assertThat(user.getUsername()).isEqualTo("test"))
                .expectNextMatches(u -> u.getUsername().equals("test"))
                .verifyComplete();
    }
}
