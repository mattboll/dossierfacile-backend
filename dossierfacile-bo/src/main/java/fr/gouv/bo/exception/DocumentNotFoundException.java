package fr.gouv.bo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(Long id) {
        super("Could not find document with id " + id);
    }

    public DocumentNotFoundException(String name) {
        super("Could not find document with name " + name);
    }
}
