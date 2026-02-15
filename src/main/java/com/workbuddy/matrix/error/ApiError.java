package com.workbuddy.matrix.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiError(
        HttpStatus status,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<ApiFieldError> errors,
        Instant timestamp
) {

    public ApiError(HttpStatus status , String message){
        this(status,message,null,Instant.now());
    }
    public ApiError(HttpStatus status ,String message , List<ApiFieldError> errors){
        this(status,message,errors,Instant.now());
    }
}

record ApiFieldError(String field ,String message){

}
