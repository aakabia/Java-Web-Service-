package com.example.demo.auth.authModel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordRequest {

    @NotBlank
    @Email
    private String email;


}

/* object for our PasswordRequest */
// I use lombok annotations to build constructors
