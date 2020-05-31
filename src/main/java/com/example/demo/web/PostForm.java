package com.example.demo.web;

import com.example.demo.domain.PersistentEntity;
import com.example.demo.domain.Username;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.example.demo.domain.Post.Status.DRAFT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostForm implements Serializable {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

}
