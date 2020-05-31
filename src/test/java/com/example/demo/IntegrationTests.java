package com.example.demo;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.Collection;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "context.initializer.classes=com.example.demo.MongodbContainerInitializer"
        }
)
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class IntegrationTests {

    @LocalServerPort
    int port;

    WebTestClient client;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setup() {
        client = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void getAllPostsWithAuthentication_ShouldBeOk() {
        client
                .get()
                .uri("/posts/")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getNoneExistedPost_ShouldReturn404() {
        client
                .get()
                .uri("/posts/ABC")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createPostWithoutAuthentication_shouldReturn401() {
        client
                .post()
                .uri("/posts")
                .body(BodyInserters.fromValue(Post.builder().title("Post test").content("content of post test").build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void updateNoneExistedPostWithUserRole_shouldReturn404() {
        client
                .mutate().filter(userJwtAuthentication()).build()
                .put()
                .uri("/posts/none_existed")
                .body(BodyInserters.fromValue(Post.builder().title("updated title").content("updated content").build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deletePostWithUserRole_shouldReturn403() {
        client
                .mutate().filter(userJwtAuthentication()).build()
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteNoneExistedPostWithAdminRole_shouldReturn404() {
        client
                .mutate().filter(adminJwtAuthentication()).build()
                .delete()
                .uri("/posts/none_existed")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void postCrudOperations() {
        int randomInt = new Random().nextInt();
        String title = "Post test " + randomInt;
        String content = "content of " + title;


        var result = client
                .mutate().filter(userJwtAuthentication()).build()
                .post()
                .uri("/posts")
                .bodyValue(Post.builder().title(title).content(content).build())
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Void.class);


        String savedPostUri = result.getResponseHeaders().getLocation().toString();

        assertNotNull(savedPostUri);

        client
                .get()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.content").isEqualTo(content)
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.createdBy.username").isEqualTo("user")
                .jsonPath("$.lastModifiedDate").isNotEmpty()
                .jsonPath("$.lastModifiedBy.username").isEqualTo("user");

        // added comment
        client
                .mutate().filter(userJwtAuthentication()).build()
                .post()
                .uri(savedPostUri + "/comments")
                .bodyValue(Comment.builder().content("my comments").build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        // get comments of post
        client
                .get()
                .uri(savedPostUri + "/comments")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Comment.class).hasSize(1);

        String updatedTitle = "updated title";
        String updatedContent = "updated content";
        client
                .mutate().filter(adminJwtAuthentication()).build()
                .put()
                .uri(savedPostUri)
                .bodyValue(Post.builder().title(updatedTitle).content(updatedContent).build())
                .exchange()
                .expectStatus().isNoContent();

        //verified updated.
        client
                .get()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(updatedTitle)
                .jsonPath("$.content").isEqualTo(updatedContent)
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.createdBy.username").isEqualTo("user")
                .jsonPath("$.lastModifiedDate").isNotEmpty()
                .jsonPath("$.lastModifiedBy.username").isEqualTo("admin");


        client
                .mutate().filter(userJwtAuthentication()).build()
                .delete()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isForbidden();

        client
                .mutate().filter(adminJwtAuthentication()).build()
                .delete()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isNoContent();

    }

    private ExchangeFilterFunction userJwtAuthentication() {
        String jwt = generateToken("user", "ROLE_USER");
        return (request, next) ->
                next.exchange(ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(jwt))
                        .build());
    }

    private ExchangeFilterFunction adminJwtAuthentication() {
        String jwt = generateToken("admin", "ROLE_ADMIN");
        return (request, next) ->
                next.exchange(ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(jwt))
                        .build());
    }

    private String generateToken(String username, String... roles) {
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roles);
        var principal = new User(username, "password", authorities);
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        var jwt = jwtTokenProvider.createToken(usernamePasswordAuthenticationToken);
        log.debug("generated jwt token::" + jwt);

        return jwt;
    }

}
