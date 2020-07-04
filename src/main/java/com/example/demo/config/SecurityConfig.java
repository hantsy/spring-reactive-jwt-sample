package com.example.demo.config;

import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtTokenAuthenticationFilter;
import com.example.demo.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http, JwtTokenProvider tokenProvider,
			ReactiveAuthenticationManager reactiveAuthenticationManager) {

		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.authenticationManager(reactiveAuthenticationManager)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.authorizeExchange(it -> it.pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
						.pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
						.pathMatchers("/posts/**").authenticated()
						.pathMatchers("/me").authenticated()
						.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
						.anyExchange().permitAll())
				.addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
				.build();

	}

	private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication,
			AuthorizationContext context) {
		return authentication.map(a -> context.getVariables().get("user").equals(a.getName()))
				.map(AuthorizationDecision::new);
	}

	@Bean
	public ReactiveUserDetailsService userDetailsService(UserRepository users) {
		return (username) -> users.findByUsername(username)
				.map(u -> User.withUsername(u.getUsername()).password(u.getPassword())
						.authorities(u.getRoles().toArray(new String[0])).accountExpired(!u.isActive())
						.credentialsExpired(!u.isActive()).disabled(!u.isActive()).accountLocked(!u.isActive())
						.build());
	}

	@Bean
	public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
		authenticationManager.setPasswordEncoder(passwordEncoder);
		return authenticationManager;
	}

}
