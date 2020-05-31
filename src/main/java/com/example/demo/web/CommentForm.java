package com.example.demo.web;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentForm {

    @NotBlank
    private String content;
}
