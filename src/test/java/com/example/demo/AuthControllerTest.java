package com.example.demo;

import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.web.AuthController;
import com.example.demo.web.AuthenticationRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AuthController.class, excludeAutoConfiguration = {
		ReactiveUserDetailsServiceAutoConfiguration.class, ReactiveSecurityAutoConfiguration.class })
@Slf4j
public class AuthControllerTest {

	@MockBean
	private JwtTokenProvider tokenProvider;

	@MockBean
	private ReactiveAuthenticationManager authenticationManager;

	@Autowired
	private WebTestClient client;

	@Test
	public void testFindByUsername() {

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("test", "password",
				AuthorityUtils.createAuthorityList("ROLE_USER"));

		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(Mono.just(usernamePasswordAuthenticationToken));
		when(this.tokenProvider.createToken(any(Authentication.class))).thenReturn("atesttoken");

		var req = AuthenticationRequest.builder().username("test").password("password").build();

		this.client.post().uri("/auth/token").body(BodyInserters.fromValue(req)).exchange().expectHeader()
				.valueEquals(HttpHeaders.AUTHORIZATION, "Bearer atesttoken").expectBody().jsonPath("$.id_token")
				.isEqualTo("atesttoken");

		verify(this.authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(this.tokenProvider, times(1)).createToken(any(Authentication.class));
		verifyNoMoreInteractions(this.authenticationManager);
		verifyNoMoreInteractions(this.tokenProvider);
	}

}
