package com.example.demo;

import com.example.demo.domain.PostId;
import com.example.demo.domain.Comment;
import com.example.demo.repository.CommentRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DataMongoTest
@Testcontainers
@Slf4j
class CommentRepositoryTest {

    @Container
    @ServiceConnection
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6");

    @Autowired
    private CommentRepository comments;

    @SneakyThrows
    @BeforeEach
    void setup() {
        log.debug("setup CommentRepositoryTest...");
        CountDownLatch latch = new CountDownLatch(1);
        this.comments.deleteAll().then()
                .then(this.comments.save(Comment.builder().content("test").post(new PostId("post-id")).build()))
                .doOnTerminate(latch::countDown)
                .subscribe();

        latch.await(1000, TimeUnit.MILLISECONDS);
    }

    @TestFactory
    List<DynamicTest> testFindByPostId() {

        return List.of(
                dynamicTest("find by post id", () -> {
                    this.comments.findByPost(new PostId("post-id"))
                            .as(StepVerifier::create)
                            .consumeNextWith(c -> assertThat(c.getContent()).isEqualTo("test"))
                            .verifyComplete();
                }),
                dynamicTest("count by post id", () -> {
                    this.comments.countByPost(new PostId("post-id"))
                            .as(StepVerifier::create)
                            .consumeNextWith(c -> assertThat(c.longValue()).isEqualTo(1L))
                            .verifyComplete();
                })
        );

    }

}
