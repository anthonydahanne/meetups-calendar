package dev.meetups;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
  
   @ExceptionHandler(ConstraintViolationException.class)
   protected ResponseEntity<JsonError> handleEntityNotFound(
           ConstraintViolationException ex) {

       String problems = ex.getConstraintViolations()
               .stream()
               .map(ConstraintViolation::getMessage)
               .collect(Collectors.joining( ";" ));
       JsonError jsonError = new JsonError("ConstraintViolationException", problems);

       return new ResponseEntity<>(jsonError, HttpStatus.BAD_REQUEST);
   }
}


record JsonError(String problem, String detailedProblem){}