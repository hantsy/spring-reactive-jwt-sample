package com.example.demo;

import com.example.demo.security.jwt.JwtTokenAuthenticationFilter;
import com.example.demo.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

public class JwtAuthenticationFilterTest {

    JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
    ServerWebExchange exchange = mock(ServerWebExchange.class, RETURNS_DEEP_STUBS);
    WebFilterChain chain = mock(WebFilterChain.class, RETURNS_DEEP_STUBS);

    @BeforeEach
    public void setup() {
        reset(tokenProvider);
        reset(exchange);
        reset(chain);
    }

    @Test
    public void testFilter() {
        var filter = new JwtTokenAuthenticationFilter(tokenProvider);

        var usernamePasswordToken = new UsernamePasswordAuthenticationToken(
                "test",
                "password",
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        when(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer atesttoken");
        when(tokenProvider.validateToken(anyString())).thenReturn(true);
        when(tokenProvider.getAuthentication(anyString())).thenReturn(usernamePasswordToken);
        when(chain.filter(exchange).subscriberContext(ReactiveSecurityContextHolder.withAuthentication(usernamePasswordToken))).thenReturn(Mono.empty());

        filter.filter(exchange, chain);

        verify(chain, times(1)).filter(exchange);
    }
}
