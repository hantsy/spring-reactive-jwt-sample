package com.example.demo;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.domain.PostId;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.web.CommentForm;
import com.example.demo.web.PostController;
import com.example.demo.web.PostForm;
import com.example.demo.web.UpdateStatusRequest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@WebFluxTest(
        controllers = PostController.class,
        excludeAutoConfiguration = {
                ReactiveUserDetailsServiceAutoConfiguration.class,
                ReactiveSecurityAutoConfiguration.class
        }
)
@Slf4j
public class PostControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    PostRepository posts;

    @MockBean
    CommentRepository comments;

    @BeforeAll
    public static void beforeAll() {
        log.debug("before all...");
    }

    @AfterAll
    public static void afterAll() {
        log.debug("after all...");
    }


    @BeforeEach
    public void beforeEach() {
        log.debug("before each...");
    }

    @AfterEach
    public void afterEach() {
        log.debug("after each...");
    }

    @Test
    public void getAllPostsWithKeyword_shouldBeOk() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        given(posts.findByTitleContains("first", pageRequest))
                .willReturn(Flux.just(Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).status(Post.Status.PUBLISHED).build()));

        client.get().uri("/posts?q=first").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("my first post")
                .jsonPath("$[0].id").isEqualTo("1")
                .jsonPath("$[0].content").isEqualTo("content of my first post");

        verify(this.posts, times(1)).findByTitleContains(anyString(), any(Pageable.class));
        verifyNoMoreInteractions(this.posts);

    }

    @Test
    public void getAllPostsWithoutKeyword_shouldBeOk() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        given(posts.findAll(pageRequest.getSort()))
                .willReturn(Flux.just(Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).status(Post.Status.PUBLISHED).build()));

        client.get().uri("/posts").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("my first post")
                .jsonPath("$[0].id").isEqualTo("1")
                .jsonPath("$[0].content").isEqualTo("content of my first post");

        verify(this.posts, times(1)).findAll(any(Sort.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void getAllPostsByKeyword_shouldBeOk() {
        List<Post> data = IntStream.range(1, 11)//15 posts will be created.
                .mapToObj(n -> Post.builder()
                        .id("" + n)
                        .title("my " + n + " blog post")
                        .content("content of my " + n + " blog post")
                        .status(Post.Status.PUBLISHED)
                        .createdDate(LocalDateTime.now())
                        .build())
                .collect(toList());

        List<Post> data2 = IntStream.range(11, 16)//5 posts will be created.
                .mapToObj(n -> Post.builder()
                        .id("" + n)
                        .title("my " + n + " blog test post")
                        .content("content of my " + n + " blog post")
                        .status(Post.Status.PUBLISHED)
                        .createdDate(LocalDateTime.now())
                        .build())
                .collect(toList());

        List<Post> data3 = List.of(5, 15, 15).stream()//3 posts will be created.
                .map(n -> Post.builder()
                        .id("" + n)
                        .title("my " + n + " blog post")
                        .content("content of my " + n + " blog post")
                        .status(Post.Status.PUBLISHED)
                        .createdDate(LocalDateTime.now())
                        .build())
                .collect(toList());
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        PageRequest pageRequest2 = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        PageRequest pageRequest3 = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));

        given(posts.findAll(pageRequest.getSort()))
                .willReturn(Flux.fromIterable(data));

        given(posts.findByTitleContains("test", pageRequest2))
                .willReturn(Flux.fromIterable(data2));

        given(posts.count())
                .willReturn(Mono.just(15L));
        given(posts.countByTitleContains("5"))
                .willReturn(Mono.just(3L));

        client.get().uri("/posts").exchange()
                .expectStatus().isOk()
                .expectBodyList(Post.class).hasSize(10);
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/posts")
                        .queryParam("page", 1)
                        .queryParam("q", "test")
                        .build()
                ).exchange()
                .expectStatus().isOk()
                .expectBodyList(Post.class).hasSize(5);

        client.get().uri("/posts/count").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.count").isEqualTo(15);

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/posts/count")
                        .queryParam("q", "5")
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.count").isEqualTo(3);

        verify(this.posts, times(1)).findAll(any(Sort.class));
        verify(this.posts, times(1)).findByTitleContains(anyString(), any(Pageable.class));
        verify(this.posts, times(1)).count();
        verify(this.posts, times(1)).countByTitleContains(anyString());
        verifyNoMoreInteractions(this.posts);

    }

    @Test
    public void getPostById_shouldBeOk() {
        given(posts.findById("1"))
                .willReturn(Mono.just(Post.builder().id("1").title("my first post").content("content of my first post").build()));

        client.get().uri("/posts/1").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("my first post")
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.content").isEqualTo("content of my first post");

        verify(this.posts, times(1)).findById(anyString());
        verifyNoMoreInteractions(this.posts);

    }

    @Test
    public void getPostByNonExistedId_shouldReturn404() {
        given(posts.findById("1"))
                .willReturn(Mono.empty());

        client.get().uri("/posts/1").exchange()
                .expectStatus().isNotFound();

        verify(this.posts, times(1)).findById(anyString());
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void updatePost_shouldBeOk() {
        Post post = Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build();

        given(posts.findById("1"))
                .willReturn(Mono.just(post));

        post.setTitle("updated title");
        post.setContent("updated content");

        given(posts.save(post))
                .willReturn(Mono.just(Post.builder().id("1").title("updated title").content("updated content").createdDate(LocalDateTime.now()).build()));

        client.put().uri("/posts/1").body(BodyInserters.fromValue(post))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(this.posts, times(1)).findById(anyString());
        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void updatePostStatus_shouldBeOk() {
        Post post = Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build();

        given(posts.findById("1"))
                .willReturn(Mono.just(post));

        post.setStatus(Post.Status.PUBLISHED);

        given(posts.save(post))
                .willReturn(Mono.just(Post.builder().id("1").title("updated title").content("updated content").createdDate(LocalDateTime.now()).build()));

        client.put().uri("/posts/1/status").body(BodyInserters.fromValue(new UpdateStatusRequest("PUBLISHED")))
                .exchange()
                .expectStatus().isNoContent();

        verify(this.posts, times(1)).findById(anyString());
        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void createPost_shouldBeOk() {
        PostForm formData = PostForm.builder().title("my first post").content("content of my first post").build();
        given(posts.save(any(Post.class)))
                .willReturn(Mono.just(Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build()));

        client.post().uri("/posts").body(BodyInserters.fromValue(formData))
                .exchange()
                .expectHeader().value("Location", CoreMatchers.containsString("/posts/1"))
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void deletePost_shouldBeOk() {
        Post post = Post.builder().id("1").title("my first post").content("content of my first post").createdDate(LocalDateTime.now()).build();

        given(posts.findById("1"))
                .willReturn(Mono.just(post));
        Mono<Void> mono = Mono.empty();
        given(posts.delete(post))
                .willReturn(mono);

        client.delete().uri("/posts/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(this.posts, times(1)).findById(anyString());
        verify(this.posts, times(1)).delete(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void getCommentsByPostId_shouldBeOk() {
        given(comments.findByPost(any(PostId.class)))
                .willReturn(Flux.just(Comment.builder().id("comment-id-1").post(new PostId("1")).content("comment of my first post").build()));

        client.get().uri("/posts/1/comments").exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(result -> log.debug("RESPONSE::" + new String(result.getResponseBody())))
                .jsonPath("$.[0].id").isEqualTo("comment-id-1")
                .jsonPath("$.[0].content").isEqualTo("comment of my first post");

        verify(this.comments, times(1)).findByPost(any(PostId.class));
        verifyNoMoreInteractions(this.comments);

    }

    @Test
    public void getCommentsCountByPostId_shouldBeOk() {
        given(comments.countByPost(any(PostId.class)))
                .willReturn(Mono.just(1L));

        client.get().uri("/posts/1/comments/count").exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(result -> log.debug("RESPONSE::" + new String(result.getResponseBody())))
                .jsonPath("$.count").isEqualTo(1L);

        verify(this.comments, times(1)).countByPost(any(PostId.class));
        verifyNoMoreInteractions(this.comments);

    }

    @Test
    public void createCommentOfPost_shouldBeOk() {

        given(comments.save(any(Comment.class)))
                .willReturn(Mono.just(Comment.builder().id("comment-id-1").post(PostId.builder().id("1").build()).content("content of my first post").createdDate(LocalDateTime.now()).build()));

        CommentForm form = CommentForm.builder().content("comment of my first post").build();
        client.post().uri("/posts/1/comments").body(BodyInserters.fromValue(form))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        verify(this.comments, times(1)).save(any(Comment.class));
        verifyNoMoreInteractions(this.comments);
    }

}
