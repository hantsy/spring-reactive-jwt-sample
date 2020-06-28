package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.UserController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                ReactiveUserDetailsServiceAutoConfiguration.class,
                ReactiveSecurityAutoConfiguration.class
        }
)
@Slf4j
public class UserControllerTest {

    @MockBean
    UserRepository users;

    @Autowired
    WebTestClient client;

    @Test
    public void testFindByUsername() {
        var user = User.builder().username("test").password("password").roles(List.of("ROLE_USER")).build();
        when(users.findByUsername(anyString()))
                .thenReturn(Mono.just(user)                );

        this.client.get().uri("/users/test")
                .exchange()
                .expectBody()
                .jsonPath("$.username").isEqualTo("test")
                .jsonPath("$.roles").isArray();

        verify(this.users, times(1)).findByUsername(anyString());
        verifyNoMoreInteractions(this.users);
    }
}
