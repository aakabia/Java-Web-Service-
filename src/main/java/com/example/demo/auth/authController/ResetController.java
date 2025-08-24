package com.example.demo.auth.authController;

import com.example.demo.auth.authModel.PasswordRequest;
import com.example.demo.auth.authService.PasswordService;
import com.example.demo.exception.customExceptions.EmailSenderException;
import com.example.demo.exception.customExceptions.UserAccountLockedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ResetController {

    private final PasswordService passwordService;

    // route to request a password change

    @PostMapping("/reset/password/request")
    public ResponseEntity<String> requestPasswordReset( @Valid @RequestBody PasswordRequest passwordRequest) throws EmailSenderException, UserAccountLockedException {

        Optional<String> optionalResponse = passwordService.requestPasswordReset(passwordRequest.getEmail());

        if(optionalResponse.isEmpty()){
            throw new EmailSenderException(" Sorry, Email Could Not be Sent");
        }

        return ResponseEntity.ok(optionalResponse.get());

    }

}
