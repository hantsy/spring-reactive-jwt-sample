package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ContextConfiguration(initializers = {MongodbContainerInitializer.class})
class PostRepositoryWithManualTestcontainersTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

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
        Post saved = this.postRepository
                .save(Post.builder().content("my test content").title("my test title").build())
                .block(Duration.ofSeconds(5));
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
                .expectNextMatches(p -> p.getTitle().equals("my test title"))
                .verifyComplete();
    }

}
