package com.example.demo.exception.customExceptions;

public class UserAccountLockedException extends Exception{

    public UserAccountLockedException(String message){
        super(message);
    }
}
