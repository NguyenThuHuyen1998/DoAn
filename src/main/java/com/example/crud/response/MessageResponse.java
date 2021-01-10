package com.example.crud.response;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class MessageResponse {
    private HttpStatus httpStatus;
    private Object message;

    public MessageResponse(HttpStatus httpStatus, Object message) {
        this.httpStatus= httpStatus;
        this.message = message;
    }

    public MessageResponse(Object message){
        this.message= message;
    }

    public MessageResponse(){}

    public Map<String, Object> getResponse(String message){
        Map<String, Object> result= new HashMap<>();
        result.put("Response", message);
        return result;
    }
}
