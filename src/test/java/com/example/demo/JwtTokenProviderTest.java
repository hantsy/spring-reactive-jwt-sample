package com.example.demo;

import com.example.demo.security.jwt.JwtProperties;
import com.example.demo.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Slf4j
public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_USER = "user";
    private static final String TEST_ROLE_NAME = "ROLE_USER";

    @BeforeEach
    public void setup() {
        JwtProperties properties = new JwtProperties();
        log.debug("jwt properties::" + properties);
        this.jwtTokenProvider = new JwtTokenProvider(properties);

        assertNotNull(this.jwtTokenProvider);
        this.jwtTokenProvider.init();
    }

    @Test
    public void testGenerateAndParseToken() {
        String token = generateToken(TEST_USER, TEST_ROLE_NAME);
        log.debug("generated jwt token::" + token);
        var auth = this.jwtTokenProvider.getAuthentication(token);
        var principal = (UserDetails) auth.getPrincipal();
        assertThat(principal.getUsername()).isEqualTo(TEST_USER);
        assertThat(principal.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList())).contains(TEST_ROLE_NAME);
    }

    @Test
    public void testGenerateAndParseToken_withoutRoles() {
        String token = generateToken(TEST_USER);
        log.debug("generated jwt token::" + token);
        var auth = this.jwtTokenProvider.getAuthentication(token);
        var principal = (UserDetails) auth.getPrincipal();
        assertThat(principal.getUsername()).isEqualTo(TEST_USER);
        assertThat(principal.getAuthorities()).isEmpty();
    }

    @Test
    public void testParseTokenException() {
        String token = "anunknowtokencannotbeparsedbyjwtprovider";
        assertThrows(JwtException.class, () -> this.jwtTokenProvider.getAuthentication(token));
        assertThatThrownBy(() -> this.jwtTokenProvider.getAuthentication(token)).isInstanceOf(JwtException.class);
    }

    @Test
    public void testValidateTokenException_failed() {
        String token = "anunknowtokencannotbeparsedbyjwtprovider";
        assertThat(this.jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    public void testValidateTokenException() {
        String token = generateToken(TEST_USER, TEST_ROLE_NAME);
        assertThat(this.jwtTokenProvider.validateToken(token)).isTrue();
    }


    private String generateToken(String username, String... roles) {
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roles);
        var principal = new User(username, "password", authorities);
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        return this.jwtTokenProvider.createToken(usernamePasswordAuthenticationToken);
    }
}
