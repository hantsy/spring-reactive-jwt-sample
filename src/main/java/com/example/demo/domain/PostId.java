package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Builder
public class PostId implements Serializable {

	private String id;

}
