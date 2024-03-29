package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
@Slf4j
class PostRepositoryTest {

    @Container
    @ServiceConnection
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6");

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setup()  {
        log.debug("setup PostRepositoryTest...");
    }

    @Test
    void testGetAllPostsByPagination() throws InterruptedException {
        List<Post> data = IntStream.range(1, 11)// 10 posts will be created.
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

        CountDownLatch latch = new CountDownLatch(1);
        this.postRepository.saveAll(data)
                .thenMany(this.postRepository.saveAll(data2))
                .doOnTerminate(latch::countDown)
                .doOnError(e -> log.debug("error: {}", e))
                .subscribe(saved -> log.debug("saved data: {}", saved));
        latch.await(5000, TimeUnit.MILLISECONDS);

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
