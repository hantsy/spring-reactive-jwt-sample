package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PostRepositoryTest {

    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer();

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @DynamicPropertySource
    private static void mongodbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl());
    }

    @BeforeEach
    private void setup() {
        this.reactiveMongoTemplate.remove(Post.class)
                .all()
                .subscribe(
                        r -> log.debug("delete all posts: " + r),
                        e -> log.debug("error: " + e),
                        () -> log.debug("done")
                );
    }

    @Test
    void testSavePost() {
        StepVerifier
                .create(this.postRepository
                        .save(Post.builder().content("my test content").title("my test title").build()))
                .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
                .expectComplete()
                .verify();
    }

    @Test
    void testSaveAndVerifyPost() {
        Post saved = this.postRepository.save(Post.builder().content("my test content").title("my test title").build())
                .block();
        assertThat(saved.getId()).isNotNull();
        assertThat(this.reactiveMongoTemplate.collectionExists(Post.class).block()).isTrue();
        assertThat(this.reactiveMongoTemplate.findById(saved.getId(), Post.class).block().getTitle())
                .isEqualTo("my test title");
    }

    @Test
    void testGetAllPost() {
        Post post1 = Post.builder().content("my test content").title("my test title").build();
        Post post2 = Post.builder().content("content of another post").title("another post title").build();

        Flux<Post> allPosts = Flux.just(post1, post2)
                .flatMap(this.postRepository::save)
                .thenMany(this.postRepository.findAll(Sort.by((Sort.Direction.ASC), "title")));

        StepVerifier.create(allPosts)
                .expectNextMatches(p -> p.getTitle().equals("another post title"))
                .expectNextMatches(p -> p.getTitle().equals("my test title")).verifyComplete();
    }

    @Test
    void testGetAllPostsByPagination() {
        List<Post> data = IntStream.range(1, 11)// 15 posts will be created.
                .mapToObj(n -> Post.builder()
                        .id("" + n)
                        .title("my " + n + " first post")
                        .content("content of my " + n + " first post")
                        .status(Post.Status.PUBLISHED)
                        .createdDate(LocalDateTime.now())
                        .build()
                )
                .collect(toList());

        List<Post> data2 = IntStream.range(11, 16)// 5 posts will be created.
                .mapToObj(n -> Post.builder()
                        .id("" + n)
                        .title("my " + n + " first test post")
                        .content("content of my " + n + " first post")
                        .status(Post.Status.PUBLISHED)
                        .createdDate(LocalDateTime.now())
                        .build()
                )
                .collect(toList());

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));

        this.postRepository.saveAll(data)
                .thenMany(this.postRepository.saveAll(data2))
                .then()
                .block();

        this.postRepository.findByTitleContains("test", pageRequest)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();

        this.postRepository.countByTitleContains("test")
                .as(StepVerifier::create)
                .consumeNextWith(c -> assertThat(c).isEqualTo(5L))
                .verifyComplete();

        this.postRepository.findAll(pageRequest.getSort())
                .as(StepVerifier::create)
                .expectNextCount(15)
                .verifyComplete();

        this.postRepository.count().as(StepVerifier::create)
                .consumeNextWith(c -> assertThat(c).isEqualTo(15L)).verifyComplete();

    }

}
