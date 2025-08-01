package com.example.demo.auth.authService;


import com.example.demo.auth.authModel.AuthenticationRequest;
import com.example.demo.auth.authModel.AuthenticationResponse;
import com.example.demo.auth.authModel.RegisterRequest;

import com.example.demo.exception.customExceptions.EmailException;
import com.example.demo.exception.customExceptions.PasswordException;
import com.example.demo.exception.customExceptions.PhoneNumberException;
import com.example.demo.exception.customExceptions.UserNameException;
import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
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



    // created a registerUser service that checks if fields from the request are valid
    // Also, it checks builds a user with the data from the request
    // Next, we add extra claims (roles) to the token
    // Last, it saves the user to our db and returns us a jwt to use on the frontend.
    public AuthenticationResponse register(RegisterRequest request, Set<Role> roles) throws EmailException, UserNameException, PasswordException, PhoneNumberException {

        Map<String, Object> extraClaims = new HashMap<>();
        // todo: add default account on user registration
        List<Account> userAccounts = new ArrayList<>();

        boolean allFieldsValidated = userFieldsValidator.areAllFieldsValidated(request);

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .userAccounts(userAccounts)
                .roles(roles)
                .build();



        extraClaims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        extraClaims.put("token_type", "ACCESS");



        userRepository.save(user);
        var jwtRefreshToken = jwtService.generateRefreshToken(user);
        var jwtToken = jwtService.generateToken(extraClaims ,user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }


    // Uses authenticationManager to verify the username and password provided during login
    // once username and password are verified, we get user from the db and generate a token for that user
    // also we add extra claims (roles) to the token
    // return a AuthenticationResponse if successfully
    // remember authenticationManager works alongside authenticationProvider
    public AuthenticationResponse login(AuthenticationRequest request){

        Map<String, Object> extraClaims = new HashMap<>();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
        ));

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> { return new UsernameNotFoundException("Sorry, this username was not found!");
                });


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



    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

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

        if(userName != null){

            var userDetails = userRepository.findByUsername(userName).orElseThrow();


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
