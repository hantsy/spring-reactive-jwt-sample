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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);

    private ServerWebExchange exchange = mock(ServerWebExchange.class, RETURNS_DEEP_STUBS);

    private WebFilterChain chain = mock(WebFilterChain.class, RETURNS_DEEP_STUBS);

    @BeforeEach
    void setup() {
        reset(this.tokenProvider);
        reset(this.exchange);
        reset(this.chain);
    }

    @Test
    void testFilter() {
        var filter = new JwtTokenAuthenticationFilter(this.tokenProvider);

        var usernamePasswordToken = new UsernamePasswordAuthenticationToken("test", "password",
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        when(this.exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer atesttoken");
        when(this.tokenProvider.validateToken(anyString())).thenReturn(true);
        when(this.tokenProvider.getAuthentication(anyString())).thenReturn(usernamePasswordToken);
        when(
                this.chain
                        .filter(this.exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(usernamePasswordToken))
        ).thenReturn(Mono.empty());

        filter.filter(this.exchange, this.chain);

        verify(this.chain, times(1)).filter(this.exchange);
    }

    @Test
    void testFilterWithNoToken() {
        var filter = new JwtTokenAuthenticationFilter(this.tokenProvider);

        when(this.exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .thenReturn(null);
        when(this.chain.filter(this.exchange)).thenReturn(Mono.empty());

        filter.filter(this.exchange, this.chain);

        verify(this.chain, times(1)).filter(this.exchange);
    }

    @Test
    void testFilterWithInvalidToken() {
        var filter = new JwtTokenAuthenticationFilter(this.tokenProvider);

        when(this.exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer atesttoken");
        when(this.tokenProvider.validateToken(anyString())).thenReturn(false);
        when(
                this.chain
                        .filter(this.exchange)
        ).thenReturn(Mono.empty());

        filter.filter(this.exchange, this.chain);

        verify(this.chain, times(1)).filter(this.exchange);
    }

}
