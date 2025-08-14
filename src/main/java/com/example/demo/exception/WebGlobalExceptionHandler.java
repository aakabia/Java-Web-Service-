package com.example.demo.exception;


import com.example.demo.exception.customExceptions.EmailSenderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WebGlobalExceptionHandler {

    @Value("${frontend.base-url}")
    private String frontEndDevUrl;

    @ExceptionHandler(EmailSenderException.class)
    public String handleEmailSenderException (EmailSenderException exception, Model model){
        model.addAttribute("FRONT_END_DEV_URL", frontEndDevUrl); // pass frontend URL
        return "email-error";
    }
}
