package com.example.crud.response;


import java.util.HashMap;
import java.util.Map;

public class MessageResponse {
    public Map<String, String> getResponse(String message){
        Map<String, String> result= new HashMap<>();
        result.put("Response", message);
        return result;
    }
}
