package org.apromore.alignment.web.controller;

import java.util.NoSuchElementException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

  private ObjectMapper mapper;

  @Autowired
  public ApiExceptionHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ResponseBody
  public JsonNode noSuchElementException(final Exception ex) {
    final ObjectNode response = mapper.createObjectNode();
    response.put("status", "404");
    response.put("message", ex.getMessage());
    log.error("Client error", ex);
    return response;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ResponseBody
  public JsonNode illegalArgumentException(final Exception ex) {
    return clientError400(ex);
  }

  private JsonNode clientError400(Exception ex) {
    final ObjectNode response = mapper.createObjectNode();
    response.put("status", "400");
    response.put("message", ex.getMessage());
    log.error("Client error", ex);
    return response;
  }
}
