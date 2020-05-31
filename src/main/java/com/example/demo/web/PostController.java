package com.example.demo.web;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.domain.PostId;
import com.example.demo.domain.PostNotFoundException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.created;

@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository posts;

    private final CommentRepository comments;


    @GetMapping("")
    public Flux<Post> all(@RequestParam(value = "q", required = false) String q,
                          @RequestParam(value = "page", defaultValue = "0") long page,
                          @RequestParam(value = "size", defaultValue = "10") long size) {
        return filterPublishedPostsByKeyword(q)
                .sort(comparing(Post::getCreatedDate).reversed())
                .skip(page * size).take(size);
    }

    @GetMapping(value = "/count")
    public Mono<CountValue> count(@RequestParam(value = "q", required = false) String q) {
        return filterPublishedPostsByKeyword(q).count().log().map(CountValue::new);
    }

    private Flux<Post> filterPublishedPostsByKeyword(String q) {
        return this.posts.findAll()
                .filter(p -> Post.Status.PUBLISHED == p.getStatus())
                .filter(
                        p -> Optional.ofNullable(q)
                                .map(key -> p.getTitle().contains(key) || p.getContent().contains(key))
                                .orElse(true)
                );
    }

    @PostMapping("")
    public Mono<ResponseEntity<Void>> create(@RequestBody @Valid Mono<PostForm> formData) {

        return formData
                .map(data ->
                        Post.builder().title(data.getTitle()).content(data.getContent()).build()
                )
                .flatMap(this.posts::save)
                .map(saved -> created(URI.create("/posts/" + saved.getId())).build());
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return this.posts.findById(id).switchIfEmpty(Mono.error(new PostNotFoundException(id)));
    }

    @PutMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> update(@PathVariable("id") String id, @RequestBody @Valid Post post) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(this.posts::save)
                .flatMap(data -> Mono.empty());
    }

    @PutMapping("/{id}/status")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> updateStatus(@PathVariable("id") String id, @RequestBody @Valid UpdateStatusRequest status) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .map(p -> {
                    // TODO: check if the current user is author it has ADMIN role.
                    p.setStatus(Post.Status.valueOf(status.getStatus()));

                    return p;
                })
                .flatMap(this.posts::save)
                .flatMap(data -> Mono.empty());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@PathVariable("id") String id) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .flatMap(this.posts::delete);
    }

    @GetMapping("/{id}/comments")
    public Flux<Comment> getCommentsOf(@PathVariable("id") String id) {
        return this.comments.findByPost(new PostId(id));
    }

    @GetMapping("/{id}/comments/count")
    public Mono<CountValue> getCommentsCountOf(@PathVariable("id") String id) {
        return this.comments.countByPost(new PostId(id)).map(CountValue::new);
    }

    @PostMapping("/{id}/comments")
    public Mono<ResponseEntity<Void>> createCommentsOf(@PathVariable("id") String id, @RequestBody @Valid CommentForm form) {
        Comment comment = Comment.builder()
                .post(new PostId(id))
                .content(form.getContent())
                .build();

        return this.comments.save(comment)
                .map(saved -> created(URI.create("/posts/" + id + "/comments/" + saved.getId())).build());
    }

}
