package com.example.demo.exception;

import com.example.demo.exception.customExceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProjectGlobalExceptions {

    // ProjectGlobalExceptions is for our @RestController global exceptions

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<String> handleEmailException (EmailException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<String> handlePasswordException (PasswordException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(UserNameException.class)
    public ResponseEntity<String> handleUserNameException (UserNameException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUserNameNotFoundException (UserNameException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(PhoneNumberException.class)
    public ResponseEntity<String> handleUserPhoneNumberException (UserNameException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(EnableUserAccountException.class)
    public ResponseEntity<String> handleEnableUserAccountException (EnableUserAccountException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException (UserNotFoundException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(UserAccountLockedException.class)
    public ResponseEntity<String> handleUserAccountLockedException (UserAccountLockedException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }


}
