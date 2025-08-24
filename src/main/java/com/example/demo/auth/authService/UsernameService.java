package com.example.demo.auth.authService;


import com.example.demo.exception.customExceptions.EmailSenderException;
import com.example.demo.exception.customExceptions.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsernameService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // sendUserNameEmail accepts a string and finds a user in our repository
    // if no user is found we throw an exception
    // send an email and return string if email is sent successfully
    @Transactional(readOnly = true)
    public String sendUserNameEmail(String email) throws UserNotFoundException, EmailSenderException {

        String template = "username-email-content.html";
        String subject = "Username Request";
        String errorMessage = "Could Not Send Username!";

        Optional<User> requestingUser = userRepository.findByEmail(email);

        System.out.println(email);

        if(requestingUser.isEmpty()){
            throw new UserNotFoundException("This email does not exist!");
        }

        User user = requestingUser.get();

        emailService.sendEmail(user,template,subject,errorMessage);

        return "Username Request Email Sent!";

    }
}
