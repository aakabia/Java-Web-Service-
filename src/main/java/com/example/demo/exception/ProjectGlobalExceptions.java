package com.example.demo.exception;

import com.example.demo.exception.customExceptions.EmailException;
import com.example.demo.exception.customExceptions.PasswordException;
import com.example.demo.exception.customExceptions.UserNameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProjectGlobalExceptions {

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

}
