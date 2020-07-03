package com.example.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements PersistentEntity, Serializable {

	@Id
	private String id;

	@NotBlank
	private String content;

	private PostId post;

	private LocalDateTime createdDate;

	private Username createdBy;

	private LocalDateTime lastModifiedDate;

	private Username lastModifiedBy;

}
