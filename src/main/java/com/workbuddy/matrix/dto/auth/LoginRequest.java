package com.workbuddy.matrix.dto.auth;

public record LoginRequest (
    String email,
    String password
){
}
