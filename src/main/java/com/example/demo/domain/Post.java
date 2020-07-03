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
public class Post implements PersistentEntity, Serializable {

	@Id
	private String id;

	@NotBlank
	private String title;

	@NotBlank
	private String content;

	@Builder.Default
	private Status status = Status.DRAFT;

	// @CreatedDate
	// @Builder.Default
	private LocalDateTime createdDate;

	// @CreatedBy
	private Username createdBy;

	// @LastModifiedDate
	private LocalDateTime lastModifiedDate;

	// @LastModifiedBy
	private Username lastModifiedBy;

	public enum Status {

		DRAFT, PUBLISHED

	}

}
