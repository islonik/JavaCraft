package my.javacraft.elastic.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ErrorExceptionHandlerTest {

    @Test
    public void testHandleError() {
        RuntimeException exception = new RuntimeException("Error Message - Runtime error!!!");

        ErrorExceptionHandler errorExceptionHandler = new ErrorExceptionHandler();
        ResponseEntity<String> response = errorExceptionHandler.handleError(exception);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertEquals("Error Message - Runtime error!!!", response.getBody());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }
}
