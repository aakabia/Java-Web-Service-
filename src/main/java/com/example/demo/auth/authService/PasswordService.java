package com.example.demo.auth.authService;

import com.example.demo.auth.authModel.PasswordChangeRequest;
import com.example.demo.exception.customExceptions.EmailSenderException;
import com.example.demo.exception.customExceptions.PasswordException;
import com.example.demo.exception.customExceptions.UserAccountLockedException;
import com.example.demo.exception.customExceptions.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.JwtService;
import com.example.demo.utilities.UserFieldsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserFieldsValidator userFieldsValidator;
    private final PasswordEncoder passwordEncoder;

    // requestPasswordReset is responsible for initiating our change of password for a user.
    // finds a user by email
    // check if already has resetVerification token in place so we don't send any extra password reset emails.
    // updates reset fields for the user
    // sends reset email and returns an Optional of a string

    @Transactional(rollbackFor = Exception.class)
    public Optional<String> requestPasswordReset(String email) throws EmailSenderException, UserAccountLockedException {

        String template = "password-content.html";
        String subject = "Reset Password Email";
        String errorMessage = "Reset Password Email Could Not Send!";
        String opaqueToken = UUID.randomUUID().toString();
        String resetCode = UUID.randomUUID().toString().replace("-","").substring(0,7);


        try {
            Optional<User> user = userRepository.findByEmail(email);

            if (user.isEmpty()) {
                throw new EmailSenderException("Email does not exists");
            }

            User userAccount = user.get();

            if(userAccount.isUserAccountLocked()){
                throw new UserAccountLockedException("Sorry, this users account is locked!");
            }


            if(jwtService.isTokenValid(userAccount.getResetVerificationToken(), userAccount)){
                throw new EmailSenderException("Please Check Email to Verify!");
            }


            userAccount.setResetOpaqueToken(opaqueToken);
            userAccount.setResetCode(resetCode);
            userAccount.setResetVerificationToken(jwtService.generateVerificationToken(userAccount));
            userRepository.save(userAccount);


            emailService.sendEmail(userAccount,template,subject,errorMessage);

            return Optional.of(String.format("Reset Password Link Successfully Sent to %s", userAccount.getEmail()));


        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();



    }


    // verifyUserAccount is used to verify if a user has a valid reset verification token
    // find the user by their opaque token so no jwt is exposed on frontend
    // if we find a valid token the user is verified
    // if we do not find a valid token the appropriate Optional string is returned

    @Transactional(readOnly = true)
    public Optional<String> verifyUserAccount(String resetOpaqueToken){

        try{
            Optional<User> user = userRepository.findByResetOpaqueToken(resetOpaqueToken);

            if(user.isEmpty()){
                return Optional.empty();
            }

            User userAccount = user.get();

            if(userAccount.getResetVerificationToken().equals("NONE") || userAccount.getResetCode().equals("NONE")){
                return Optional.of("NO TOKEN PRESENT!");
            } else if(!jwtService.isTokenValid(userAccount.getResetVerificationToken(), userAccount)){
                return Optional.of("TOKEN EXPIRED!");
            }


            return  Optional.of("USER VERIFIED!");


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    // passwordVerificationResponse is used to render what template to present to the user
    // check the value of message
    // depending on the value of message we render the appropriate template
    // since we are returning templates this function will be used in a class annotated with @Controller
    // reason being is the @Controller does not automatically have the request body in the response
    // method is used within PasswordController

    @Transactional(readOnly = true)
    public String passwordVerificationResponse (Model model, String message, String token){


        if(message.equals("TOKEN EXPIRED!")){
            Optional<User> user = userRepository.findByResetOpaqueToken(token);

            if(user.isPresent()){
                User userToVerify = user.get();
                model.addAttribute("user", userToVerify);
                return "password-reset-email-expired";

            }

        } else if (message.equals("NO TOKEN PRESENT!")) {
            return "password-reset-resend-failed";

        }


        return "password-reset";


    }


    // updatePasswordWithToken is responsible for actually updating our password field
    // find user by their personal verification code
    // check if the new password and reset verification token are valid
    // if valid, we encrypt the new password and set it
    // update the reset fields and save the user
    // return true
    // catch all checked exceptions and return false for our controller to handle

    @Transactional(rollbackFor = Exception.class)
    public boolean updatePasswordWithToken (PasswordChangeRequest request){

        try{
        Optional<User> requestedUser = userRepository.findByResetCode(request.getVerificationCode());

        if(requestedUser.isEmpty()){
            throw new UserNotFoundException("Sorry, A User With this Reset Code does not exist!");

        }

        User userToChangePassword = requestedUser.get();
        boolean isPasswordValid = userFieldsValidator.passwordValidator(request);
        boolean isJwtValid = jwtService.isTokenValid(userToChangePassword.getResetVerificationToken(), userToChangePassword);

        if(!isPasswordValid || !isJwtValid){
            throw new PasswordException("Invalid or expired reset token!");
        }

        userToChangePassword.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userToChangePassword.setResetVerificationToken("NONE");
        userToChangePassword.setResetOpaqueToken("NONE");
        userToChangePassword.setResetCode("NONE");
        userRepository.save(userToChangePassword);


        return true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

    }



}
