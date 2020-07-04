package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@ToString
public class Username implements Serializable {

	private String username;

}
