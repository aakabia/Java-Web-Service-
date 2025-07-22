package com.example.demo.auth.authService;


import com.example.demo.auth.authModel.AuthenticationRequest;
import com.example.demo.auth.authModel.AuthenticationResponse;
import com.example.demo.auth.authModel.RegisterRequest;

import com.example.demo.exception.customExceptions.EmailException;
import com.example.demo.exception.customExceptions.PasswordException;
import com.example.demo.exception.customExceptions.UserNameException;
import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import com.example.demo.utilities.UserFieldsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public AuthenticationResponse registerUser(RegisterRequest request) throws EmailException, UserNameException, PasswordException {

        List<Account> userAccounts = new ArrayList<>();
        Map<String, Object> extraClaims = new HashMap<>();

        boolean allFieldsValidated = userFieldsValidator.areAllFieldsValidated(request);

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .userAccounts(userAccounts)
                .roles(Set.of(Role.USER))
                .build();

        userRepository.save(user);


        // add roles to the token by extracting the authority from each object within the collection
        extraClaims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        var jwtToken = jwtService.generateToken(extraClaims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    // Does the same as registerUser but for Admin users only
    public AuthenticationResponse registerAdmin(RegisterRequest request) throws UserNameException, PasswordException, EmailException {


        Map<String, Object> extraClaims = new HashMap<>();

        boolean allFieldsValidated = userFieldsValidator.areAllFieldsValidated(request);

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER, Role.ADMIN))
                .build();
        userRepository.save(user);




        extraClaims.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());


        var jwtToken = jwtService.generateToken(extraClaims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
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

        var jwtToken = jwtService.generateToken(extraClaims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }




}
