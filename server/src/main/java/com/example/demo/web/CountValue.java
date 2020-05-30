package com.example.demo.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class CountValue implements Serializable {

    private long count;
}
