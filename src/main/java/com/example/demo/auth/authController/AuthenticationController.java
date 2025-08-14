package com.example.demo.auth.authController;


import com.example.demo.auth.authModel.AuthenticationRequest;
import com.example.demo.auth.authModel.AuthenticationResponse;
import com.example.demo.auth.authModel.RegisterRequest;
import com.example.demo.auth.authService.AuthenticationService;
import com.example.demo.auth.authService.VerificationService;
import com.example.demo.exception.customExceptions.*;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;


    // register new users post route
    @PostMapping("/register/user")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) throws EmailException, PasswordException, UserNameException, PhoneNumberException, EmailSenderException {

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);

        return ResponseEntity.ok(authenticationService.register(request, roles));

    }

    // register new admin users post route
    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody RegisterRequest request) throws EmailException, PasswordException, UserNameException, PhoneNumberException, EmailSenderException {

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        roles.add(Role.ADMIN);

        return ResponseEntity.ok(authenticationService.register(request,roles));

    }


    // login post route
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) throws EnableUserAccountException, EmailSenderException {

        return ResponseEntity.ok(authenticationService.login(request));

    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, EnableUserAccountException {

        authenticationService.refreshToken(request, response);

    }

}
