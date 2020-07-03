package com.example.demo.domain;

import java.time.LocalDateTime;

public interface PersistentEntity {

	String getId();

	void setId(String id);

	Username getCreatedBy();

	void setCreatedBy(Username username);

	Username getLastModifiedBy();

	void setLastModifiedBy(Username username);

	LocalDateTime getCreatedDate();

	void setCreatedDate(LocalDateTime createdDate);

	LocalDateTime getLastModifiedDate();

	void setLastModifiedDate(LocalDateTime lastModifiedDate);

}
