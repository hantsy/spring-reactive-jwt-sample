package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    private void setup() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        this.reactiveMongoTemplate.remove(Post.class)
                .all()
                .doOnTerminate(latch::countDown)
                .subscribe(
                        r -> log.debug("delete all posts: " + r),
                        e -> log.debug("error: " + e),
                        () -> log.debug("done")
                );
        latch.await(1000, TimeUnit.MILLISECONDS);
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
