package org.games.xlspaceship.impl;

import org.games.xlspaceship.impl.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface RestResources {

    String BASE_PATH = "/xl-spaceship";

    String USER_PATH = BASE_PATH + "/user";
    String PROTOCOL_PATH = BASE_PATH + "/protocol";

    static ResponseEntity<ErrorResponse> jsonError(String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse();
        error.setError(message);
        return new ResponseEntity<>(error, status);
    }

}
