package ai.terraframe.kaleidoscope.dggs.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;

@ControllerAdvice
public class ExceptionHandlerAdvice
{

  @ExceptionHandler(GenericRestException.class)
  public ResponseEntity<?> handleException(GenericRestException e)
  {
    // log exception
    return ResponseEntity.status(e.getStatus()).body(e.getMessage());
  }
}
