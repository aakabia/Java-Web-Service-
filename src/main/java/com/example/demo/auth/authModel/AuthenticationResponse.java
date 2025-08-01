package com.example.demo.auth.authModel;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String refreshToken;

}

/* object for our AuthenticationResponse */
// I use lombok annotations to build constructors
