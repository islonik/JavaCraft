package my.javacraft.soap2rest.rest.app.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ErrorResource {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleError(Throwable ex) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatusCode.valueOf(500));
    }
}
