package com.example.demo.utilities;


import com.example.demo.auth.authModel.RegisterRequest;
import com.example.demo.exception.customExceptions.EmailException;
import com.example.demo.exception.customExceptions.PasswordException;
import com.example.demo.exception.customExceptions.PhoneNumberException;
import com.example.demo.exception.customExceptions.UserNameException;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



@RequiredArgsConstructor
@Service
public class UserFieldsValidator {


    private final UserRepository userRepository;


     public boolean areAllFieldsValidated(RegisterRequest request) throws EmailException, UserNameException, PasswordException, PhoneNumberException {


        Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])(.+)$");
        Pattern phoneNumberPattern = Pattern.compile("^(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$");


        /* validate email */
        if(request.getEmail() == null){
            throw new EmailException("Please provide a email!");
        }

        EmailValidator validator = EmailValidator.getInstance();
        boolean userEmailExist = userRepository.existsUserByEmail(request.getEmail());


        if (!validator.isValid(request.getEmail())){
            throw new EmailException("Invalid Email, please use a valid email format!");
        } else if(userEmailExist){
            throw new EmailException("This email already exists. Please login!");
        }


         /* validate userName */

         if(request.getUsername() == null){
             throw new UserNameException("Please provide a userName!");
         }

         boolean userNameExists = userRepository.existsUserByUsername(request.getUsername());

         if(request.getUsername().length() < 7 ){
             throw new UserNameException("Username must be length must be at least 7 chars long!");
         }else if(request.getUsername().length() > 10){
             throw new UserNameException("Username length must be less than 10 chars long!");
         }else if(userNameExists){
             throw new UserNameException("Username already exists, please try another!");
         }


         /* validate password */
         if(request.getPassword() == null){
             throw new PasswordException("Please provide a password!");
         }

         Matcher matcher = pattern.matcher(request.getPassword());
         boolean matchFound = matcher.matches();


         if(request.getPassword().length() < 8 ){
             throw new PasswordException("Password length must be at least 8 chars long!");
         }else if(request.getPassword().length() > 15){
             throw new PasswordException("Password length must be less than 15 chars long!");
         }
         else if(!matchFound){
             throw new PasswordException("Password must include at least one special char, uppercase letter and number!");
         }


         /* validate first and last name */
         if(request.getFirstName() == null || request.getLastName() == null){
             throw new UserNameException("Please provide a first and last name!");
         }
         else if (request.getFirstName().length() < 3 || request.getLastName().length() < 3){
             throw new UserNameException("First and last name must be at least 3 chars long! ");
         }


         /* validate phone number */
         if(request.getPhoneNumber() == null){
             throw new PhoneNumberException("Please provide a valid phone number!");
         }

         Matcher phoneNumberMatcher = phoneNumberPattern.matcher(request.getPhoneNumber());
         boolean phoneMatchFound = phoneNumberMatcher.matches();

         if(!phoneMatchFound){
             throw new PhoneNumberException("Please provide a valid phone number!");
         }

         return true;


     }




}
