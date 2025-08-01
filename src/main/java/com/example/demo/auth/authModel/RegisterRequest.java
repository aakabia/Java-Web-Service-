package com.example.demo.auth.authModel;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;

    /* below for mfa authentication*/
//    private boolean mfaEnabled;


}

/* object for our RegisterRequest */
// I use lombok annotations to build constructors
