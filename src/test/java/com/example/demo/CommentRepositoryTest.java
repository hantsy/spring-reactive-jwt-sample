package com.example.demo;

import com.example.demo.domain.Comment;
import com.example.demo.domain.PostId;
import com.example.demo.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CommentRepositoryTest {

	@Container
	private static MongoDBContainer mongoDBContainer = new MongoDBContainer();

	@Autowired
	private CommentRepository comments;

	@DynamicPropertySource
	private static void mongodbProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl());
	}

	@BeforeEach
	public void setup() {
		this.comments.deleteAll().then()
				.then(this.comments.save(Comment.builder().content("test").post(new PostId("post-id")).build()))
				.block();
	}

	@Test
	public void testFindByPostId() {
		this.comments.findByPost(new PostId("post-id")).as(StepVerifier::create)
				.consumeNextWith(c -> assertThat(c.getContent()).isEqualTo("test")).verifyComplete();
	}

	@Test
	public void testAddComment() {
		this.comments.countByPost(new PostId("post-id")).as(StepVerifier::create)
				.consumeNextWith(c -> assertThat(c.longValue()).isEqualTo(1L)).verifyComplete();
	}

}
