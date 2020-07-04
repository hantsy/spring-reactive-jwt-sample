package com.example.demo.domain;

import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class PersistentEntityCallback implements ReactiveBeforeConvertCallback<PersistentEntity> {

    @Override
    public Publisher<PersistentEntity> onBeforeConvert(PersistentEntity entity, String collection) {

        var user = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(it -> it != null && it.isAuthenticated())
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .map(userDetails -> new Username(userDetails.getUsername()))
                .switchIfEmpty(Mono.empty());

        var currentTime = LocalDateTime.now();

        if (entity.getId() == null) {
            entity.setCreatedDate(currentTime);
        }
        entity.setLastModifiedDate(currentTime);

        return user.map(u -> {
            if (entity.getId() == null) {
                entity.setCreatedBy(u);
            }
            entity.setLastModifiedBy(u);

            return entity;
        }).defaultIfEmpty(entity);
    }

}
