package com.example.demo.auth.authModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequest {
    private String verificationCode;
    private String newPassword;
    private String confirmationPassword;

}

/* object for our PasswordChangeRequest */
// I use lombok annotations to build constructors