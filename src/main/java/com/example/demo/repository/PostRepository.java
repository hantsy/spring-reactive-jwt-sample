package com.example.demo.repository;

import com.example.demo.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository extends ReactiveMongoRepository<Post, String> {

	Flux<Post> findByTitleContains(String q, Pageable pageable);

	Mono<Long> countByTitleContains(String q);

}
