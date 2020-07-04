package com.example.demo.web;

import com.example.demo.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author hantsy
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final JwtTokenProvider tokenProvider;

	private final ReactiveAuthenticationManager authenticationManager;

	@PostMapping("/token")
	public Mono<ResponseEntity> login(@Valid @RequestBody Mono<AuthenticationRequest> authRequest) {
		// @formatter:off
		return authRequest
				.flatMap(login -> this.authenticationManager
						.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()))
						.map(this.tokenProvider::createToken)
				)
				.map(jwt -> {
					HttpHeaders httpHeaders = new HttpHeaders();
					httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
					var tokenBody = Map.of("id_token", jwt);
					return new ResponseEntity<>(tokenBody, httpHeaders, HttpStatus.OK);
				});
		// @formatter:on
	}

}
