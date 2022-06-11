package com.route.datasource.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestResponse<T> {
  public T result;
  public HttpStatus status;
  
}
