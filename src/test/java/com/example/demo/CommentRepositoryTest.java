package com.example.demo;

import com.example.demo.domain.Comment;
import com.example.demo.domain.PostId;
import com.example.demo.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DataMongoTest
@Slf4j
class CommentRepositoryTest {

    @Autowired
    private CommentRepository comments;

    @BeforeEach
    private void setup() throws InterruptedException {
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
