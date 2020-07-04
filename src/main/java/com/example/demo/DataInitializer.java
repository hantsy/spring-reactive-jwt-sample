package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * @author hantsy
 */
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer {

	private final PostRepository posts;

	private final UserRepository users;

	private final PasswordEncoder passwordEncoder;

	@EventListener(value = ApplicationReadyEvent.class)
	public void init() {
		log.info("start data initialization...");
		
		//@formatter:off
		var initPosts = this.users.deleteAll()
			.thenMany(
					Flux.just("user", "admin")
					.flatMap(username -> {
						List<String> roles = "user".equals(username) ?
								Arrays.asList("ROLE_USER"): Arrays.asList("ROLE_USER", "ROLE_ADMIN");

						User user = User.builder()
								.roles(roles)
								.username(username)
								.password(passwordEncoder.encode("password"))
								.email(username + "@example.com")
								.build();

						return this.users.save(user);
					})
			);
		//@formatter:on

		//@formatter:off
		var initUsers = this.posts.deleteAll()
			.thenMany(
					Flux.just("Post one", "Post two")
						.flatMap(title ->
							this.posts.save(Post.builder()
								.title(title)
								.content("content of " + title)
								.status(Post.Status.PUBLISHED)
								.build()
							)
						)
			);
		//@formatter:on

		//@formatter:off
		initPosts.doOnSubscribe(data -> log.info("data:" + data))
			.thenMany(initUsers)
			.subscribe(
				data -> log.info("data:" + data), err -> log.error("error:" + err),
				() -> log.info("done initialization...")
			);
		//@formatter:on
	}

}
