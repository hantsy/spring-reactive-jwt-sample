package com.example.demo.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hantsy
 */
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class CurrentUserController {

    @GetMapping()
    public Mono<Map> current(@AuthenticationPrincipal Mono<Principal> principal) {
        return principal
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", user.getName());
                    map.put("roles", AuthorityUtils.authorityListToSet(((Authentication) user)
                            .getAuthorities()));
                    return map;
                });
    }
}
