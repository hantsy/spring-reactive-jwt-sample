package com.example.demo;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Slf4j
@DisplayName("API endpoints integration tests")
class IntegrationTests {

        @LocalServerPort
        private int port;

        private WebTestClient client;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @BeforeEach
        private void setup() {
                this.client = WebTestClient.bindToServer()
                                .baseUrl("http://localhost:" + port).build();
        }

        @Nested
        @DisplayName("if user is not logged in")
        class NotLoggedIn {

                @Test
                @DisplayName("should be ok when getting all posts")
                void shouldBeOkWhenGettingAllPosts() {
                        client.get().uri("/posts/").exchange().expectStatus().isOk();
                }

                @Test
                @DisplayName("should return 404 when getting a none existing post")
                void shouldReturn404WhenGettingNoneExistingPost() {
                        client.get().uri("/posts/ABC").exchange().expectStatus()
                                        .isNotFound();
                }

                @Test
                @DisplayName("should return 401 when trying to create a new post")
                void shouldBe401WhenCreatingPost() {
                        client.post().uri("/posts")
                                        .body(BodyInserters.fromValue(Post.builder()
                                                        .title("Post test")
                                                        .content("content of post test")
                                                        .build()))
                                        .exchange().expectStatus()
                                        .isEqualTo(HttpStatus.UNAUTHORIZED);
                }

                @Test
                @DisplayName("should return 401 when trying to update a post")
                void shouldReturn401WhenUpdatingPost() {
                        client.put().uri("/posts/apost")
                                        .body(BodyInserters.fromValue(Post.builder()
                                                        .title("updated title")
                                                        .content("updated content")
                                                        .build()))
                                        .exchange().expectStatus()
                                        .isEqualTo(HttpStatus.UNAUTHORIZED);
                }

                @Test
                @DisplayName("should return 401 when trying to delete a post")
                void shouldReturn401WhenDeletingPost() {
                        client.delete().uri("/posts/apost").exchange().expectStatus()
                                        .isEqualTo(HttpStatus.UNAUTHORIZED);
                }
        }

        @Nested
        @DisplayName("if user is logged in as (USER)")
        class LoggedInAsUser {

                @BeforeEach
                void setup() {
                        client = client.mutate().filter(userJwtAuthentication()).build();
                }

                @Test
                @DisplayName("should return 422 when creating a new post with empty body")
                void shouldBe422WhenCreatingPostWithEmptyBody() {
                        client.post().uri("/posts")
                                        .body(BodyInserters.fromValue(
                                                        Post.builder().build()))
                                        .exchange().expectStatus()
                                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                }

                @Test
                @DisplayName("should return 404 when trying to update a none existing post")
                void shouldReturn404WhenUpdatingNoneExistingPost() {
                        client.put().uri("/posts/none_existed")
                                        .body(BodyInserters.fromValue(Post.builder()
                                                        .title("updated title")
                                                        .content("updated content")
                                                        .build()))
                                        .exchange().expectStatus()
                                        .isEqualTo(HttpStatus.NOT_FOUND);
                }

                @Test
                @DisplayName("should return 403 when trying to delete a post")
                void shouldReturn403WhenUpdatingPost() {
                        client.delete().uri("/posts/1").exchange().expectStatus()
                                        .isEqualTo(HttpStatus.FORBIDDEN);
                }

                @Test
                @DisplayName("should work when performing the crud flow(creating, updating and deleting posts and comments)")
                void postCrudOperations() {
                        int randomInt = new Random().nextInt();
                        String title = "Post test " + randomInt;
                        String content = "content of " + title;

                        var result = client.post().uri("/posts")
                                        .bodyValue(Post.builder().title(title)
                                                        .content(content).build())
                                        .exchange().expectStatus().isCreated()
                                        .returnResult(Void.class);

                        String savedPostUri = result.getResponseHeaders().getLocation()
                                        .toString();

                        assertNotNull(savedPostUri);

                        client.get().uri(savedPostUri).exchange().expectStatus().isOk()
                                        .expectBody().jsonPath("$.title").isEqualTo(title)
                                        .jsonPath("$.content").isEqualTo(content)
                                        .jsonPath("$.createdDate").isNotEmpty()
                                        .jsonPath("$.createdBy.username")
                                        .isEqualTo("user").jsonPath("$.lastModifiedDate")
                                        .isNotEmpty()
                                        .jsonPath("$.lastModifiedBy.username")
                                        .isEqualTo("user");

                        // added comment
                        client.post().uri(savedPostUri + "/comments")
                                        .bodyValue(Comment.builder()
                                                        .content("my comments").build())
                                        .exchange().expectStatus().isCreated()
                                        .expectBody().isEmpty();

                        // get comments of post
                        client.get().uri(savedPostUri + "/comments").exchange()
                                        .expectStatus().isOk()
                                        .expectBodyList(Comment.class).hasSize(1);

                        String updatedTitle = "updated title";
                        String updatedContent = "updated content";
                        client.put().uri(savedPostUri)
                                        .bodyValue(Post.builder().title(updatedTitle)
                                                        .content(updatedContent).build())
                                        .exchange().expectStatus().isNoContent();

                        // verified updated.
                        client.get().uri(savedPostUri).exchange().expectStatus().isOk()
                                        .expectBody().jsonPath("$.title")
                                        .isEqualTo(updatedTitle).jsonPath("$.content")
                                        .isEqualTo(updatedContent)
                                        .jsonPath("$.createdDate").isNotEmpty()
                                        .jsonPath("$.createdBy.username")
                                        .isEqualTo("user").jsonPath("$.lastModifiedDate")
                                        .isNotEmpty()
                                        .jsonPath("$.lastModifiedBy.username")
                                        .isEqualTo("user");

                        // delete , user role is forbidden
                        client.delete().uri(savedPostUri).exchange().expectStatus()
                                        .isForbidden();

                }

        }

        @Nested
        @DisplayName("if user is logged in as (ADMIN)")
        class LoggedInAsAdmin {

                @BeforeEach
                void setup() {
                        client = client.mutate().filter(adminJwtAuthentication()).build();
                }

                @Test
                @DisplayName("should return 404 when trying to delete a none exiting post")
                void shouldReturn404WhenDeletingNoneExistingPost() {
                        client.delete().uri("/posts/1").exchange().expectStatus()
                                        .isEqualTo(HttpStatus.NOT_FOUND);
                }
        }

        // see:
        // https://stackoverflow.com/questions/62787995/stackoverflow-when-retrieving-jwt-token-in-webtestclient-and-seting-it-in-exchan
        /*
         * private ExchangeFilterFunction userJwtAuthentication() { return
         * ExchangeFilterFunction.ofRequestProcessor( request -> generateToken("user")
         * .map(jwt -> ClientRequest.from(request) .headers(headers ->
         * headers.setBearerAuth(jwt)) .build() ) ); }
         *
         * private ExchangeFilterFunction adminJwtAuthentication() { return
         * ExchangeFilterFunction.ofRequestProcessor( request -> generateToken("admin")
         * .map(jwt -> ClientRequest.from(request) .headers(headers ->
         * headers.setBearerAuth(jwt)) .build() ) ); }
         *
         * private Mono<String> generateToken(String username) { return this.client
         * .post().uri("/auth/login")
         * .bodyValue(AuthenticationRequest.builder().username(username).password(
         * "password").build()) .exchange() .returnResult(new
         * ParameterizedTypeReference<Map<String, String>>() { }) .getResponseBody()
         * .last() .map(d -> d.get("access_token")) .doOnSubscribe( jwt ->
         * log.debug("generated jwt token::" + jwt) );
         *
         * }
         */

        /*
         * private ExchangeFilterFunction userJwtAuthentication() { String jwt =
         * generateToken("user"); return (request, next) -> next
         * .exchange(ClientRequest.from(request).headers(headers ->
         * headers.setBearerAuth(jwt)).build()); }
         *
         * private ExchangeFilterFunction adminJwtAuthentication() { String jwt =
         * generateToken("admin"); return (request, next) -> next
         * .exchange(ClientRequest.from(request).headers(headers ->
         * headers.setBearerAuth(jwt)).build()); }
         *
         * private String generateToken(String username) { FluxExchangeResult<Map<String,
         * String>> idToken = this.client .post().uri("/auth/login")
         * .bodyValue(AuthenticationRequest.builder().username(username).password(
         * "password").build()) .exchange() .returnResult(new
         * ParameterizedTypeReference<Map<String, String>>() { }); var jwt =
         * idToken.getResponseBody().blockLast(Duration.ofSeconds(5)).get("access_token");
         * log.debug("generated jwt token::" + jwt);
         *
         * return jwt; }
         */

        private ExchangeFilterFunction userJwtAuthentication() {
                String jwt = generateToken("user", "ROLE_USER");
                return (request, next) -> next.exchange(ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(jwt)).build());
        }

        private ExchangeFilterFunction adminJwtAuthentication() {
                String jwt = generateToken("admin", "ROLE_ADMIN");
                return (request, next) -> next.exchange(ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(jwt)).build());
        }

        private String generateToken(String username, String... roles) {
                Collection<? extends GrantedAuthority> authorities = AuthorityUtils
                                .createAuthorityList(roles);
                var principal = new User(username, "password", authorities);
                var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                principal, null, authorities);

                var jwt = jwtTokenProvider
                                .createToken(usernamePasswordAuthenticationToken);
                log.debug("generated jwt token::" + jwt);

                return jwt;
        }
}
