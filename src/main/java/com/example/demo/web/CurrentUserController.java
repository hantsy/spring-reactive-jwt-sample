package com.example.demo.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author hantsy
 */
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class CurrentUserController {

	@GetMapping()
	public Mono<Map<String, Object>> current(@AuthenticationPrincipal Mono<UserDetails> principal) {
		return principal.map(user -> Map.of("name", user.getUsername(), "roles",
				AuthorityUtils.authorityListToSet(user.getAuthorities())));
	}

}
