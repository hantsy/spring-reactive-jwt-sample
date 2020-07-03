package com.example.demo.repository;

import com.example.demo.domain.Comment;
import com.example.demo.domain.PostId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {

	// @Tailable
	Flux<Comment> findByPost(PostId id);

	Mono<Long> countByPost(PostId id);

}
