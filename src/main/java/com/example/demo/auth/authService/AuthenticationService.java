package com.example.demo.auth.authService;


import com.example.demo.auth.authModel.AuthenticationRequest;
import com.example.demo.auth.authModel.AuthenticationResponse;
import com.example.demo.auth.authModel.RegisterRequest;

import com.example.demo.exception.customExceptions.*;
import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.JwtService;
import com.example.demo.utilities.UserFieldsValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AuthenticationService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserFieldsValidator userFieldsValidator;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;



    // created a registerUser service that checks if fields from the request are valid
    // Also, it checks builds a user with the data from the request
    // Next, we add extra claims (roles) to the token
    // Last, it saves the user to our db and returns us a jwt to use on the frontend.


    @Transactional(rollbackFor = Exception.class)
    public String register(RegisterRequest request, Set<Role> roles) throws EmailException, UserNameException, PasswordException, PhoneNumberException, EmailSenderException {

        Map<String, Object> extraClaims = new HashMap<>();
        // todo: add default account on user registration

        List<Account> userAccounts = new ArrayList<>();
        String template = "email-content.html";
        String subject = "Verification Email";
        String errorMessage = "Email Verification Could Not Send!";
        String opaqueToken = UUID.randomUUID().toString();

        boolean allFieldsValidated = userFieldsValidator.areAllFieldsValidated(request);

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .userAccountEnabled(false)
                .userAccountLocked(false)
                .opaqueToken(opaqueToken)
                .resetVerificationToken("NONE")
                .resetCode("NONE")
                .resetOpaqueToken("NONE")
                .userAccounts(userAccounts)
                .roles(roles)
                .build();


        var jwtVerificationToken = jwtService.generateVerificationToken(user);
        user.setVerificationToken(jwtVerificationToken);

        userRepository.save(user);

        emailService.sendEmail(user,template,subject,errorMessage);

         return String.format("User Registration Successful for %s", user.getEmail());
    }



    // Uses authenticationManager to verify the username and password provided during login
    // once username and password are verified, we get user from the db and generate a token for that user
    // make sure user account is enabled/verified
    // also we add extra claims (roles) to the token
    // return a AuthenticationResponse if successfully
    // remember authenticationManager works alongside authenticationProvider

    @Transactional
    public AuthenticationResponse login(AuthenticationRequest request) throws EnableUserAccountException, EmailSenderException {

        Map<String, Object> extraClaims = new HashMap<>();
        String template = "email-content.html";
        String subject = "Verification Email";
        String errorMessage = "Email Verification Could Not Send!";


        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> { return new UsernameNotFoundException("Sorry, this username was not found!");
                });


        // todo: check if user account locked


        if(!user.isUserAccountEnabled() && !jwtService.isTokenValid(user.getVerificationToken(),user)){

            var jwtVerificationToken = jwtService.generateVerificationToken(user);
            user.setVerificationToken(jwtVerificationToken);

            userRepository.save(user);
            emailService.sendEmail(user,template,subject,errorMessage);
            throw new EnableUserAccountException(" New Verification Email Sent, Please Verify Your Account!");

        }else if(!user.isUserAccountEnabled()){
            throw new EnableUserAccountException("Verification Email Already Sent, Please Verify Your Account!");
        }


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                ));


        extraClaims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        extraClaims.put("token_type", "ACCESS");

        var jwtRefreshToken = jwtService.generateRefreshToken(user);


        var jwtToken = jwtService.generateToken(extraClaims,user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }


    @Transactional(readOnly = true)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, EnableUserAccountException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwtRefreshToken;
        final String userName;
        Map<String, Object> extraClaims = new HashMap<>();



        // If the header does not exist return
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return;
        }

        // extract refreshToken from authHeader
        jwtRefreshToken = authHeader.substring(7);

        userName = jwtService.extractUsername(jwtRefreshToken);

        // check if username from jwtRefresh token exists
        // if exists we find the associated user from our db
        // verify that user account is enabled

        if(userName != null){

            var userDetails = userRepository.findByUsername(userName).orElseThrow();


            if(!userDetails.isUserAccountEnabled()){
                throw new EnableUserAccountException("Please Verify Your Account!");
            }

            // validate the token
            // create a new token with roles set
            // generate an auth response object
            // use  ObjectMapper().writeValue to send back to send HttpServletResponse back to user
            if(jwtService.isTokenValid(jwtRefreshToken, userDetails)){

                extraClaims.put("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
                extraClaims.put("token_type", "ACCESS");


                var jwtToken = jwtService.generateToken(extraClaims, userDetails);
                var authResponse = AuthenticationResponse.builder()
                        .token(jwtToken)
                        .refreshToken(jwtRefreshToken)
                        .build();

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);

            }

        }
    }




}
