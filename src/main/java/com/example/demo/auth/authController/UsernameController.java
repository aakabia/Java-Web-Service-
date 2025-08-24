package com.example.demo.auth.authController;


import com.example.demo.auth.authModel.PasswordRequest;
import com.example.demo.auth.authService.UsernameService;
import com.example.demo.exception.customExceptions.EmailSenderException;
import com.example.demo.exception.customExceptions.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
public class UsernameController {

    private final UsernameService usernameService;


    // retrieve username for user controller
    @PostMapping("/username/recovery")
    public ResponseEntity<String> recoverUsername(@RequestBody PasswordRequest request) throws UserNotFoundException, EmailSenderException {

        String response = usernameService.sendUserNameEmail(request.getEmail());

        return ResponseEntity.ok(response);


    }

}
