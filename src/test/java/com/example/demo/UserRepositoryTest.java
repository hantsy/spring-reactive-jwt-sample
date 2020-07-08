package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
class UserRepositoryTest {

    @Autowired
    private UserRepository users;

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
