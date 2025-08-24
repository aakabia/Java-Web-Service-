package com.example.demo.auth.authController;


import com.example.demo.auth.authModel.PasswordChangeRequest;
import com.example.demo.auth.authService.PasswordService;
import com.example.demo.exception.customExceptions.EmailSenderException;
import com.example.demo.model.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PasswordController {


    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordService passwordService;


    @Value("${frontend.base-url}")
    private String frontEndDevUrl;

    @Value("${backend.base-url}")
    private String backEndDevUrl;


    // reset password route that is used within email template "password-content.html"

    @GetMapping("/reset/password")
    public String verifyToken(@RequestParam(required = true, name = "token", defaultValue = "NONE") String token, Model model){

        Optional<String> message = passwordService.verifyUserAccount(token);

        if(message.isEmpty() || token.equals("NONE")){
            return "password-reset-resend-failed";
        }

        String verificationMessage = message.get();

        return passwordService.passwordVerificationResponse( model,verificationMessage,token);


    }


    // reset password resend route that is used within email template "password-reset-email-expired.html"

    @GetMapping("/reset/password/resend")
    public String resendEmail(@RequestParam(required = true, name = "token", defaultValue = "NONE") String token, Model model) throws EmailSenderException {

        Optional<User> user = emailService.resendPasswordEmail(token);

        if(user.isEmpty() || token.equals("NONE")){
            model.addAttribute("FRONT_END_DEV_URL", frontEndDevUrl );
            return "password-reset-resend-failed";
        }

        User requestingUser = user.get();

        if(!jwtService.isTokenValid(requestingUser.getResetVerificationToken(), requestingUser)){
            model.addAttribute("FRONT_END_DEV_URL", frontEndDevUrl );
            return "password-reset-resend-failed";
        }


        model.addAttribute("user", requestingUser );
        model.addAttribute("FRONT_END_DEV_URL", frontEndDevUrl );

        return "resend-password-success";

    }

    // password reset failed route that is used to render template "password-reset-failed.html"
    // Model used to pass attributes to the template

    @GetMapping("/password-reset-failed")
    public String showPasswordResetFailed( Model model) {

        model.addAttribute("FRONT_END_DEV_URL", frontEndDevUrl );

        return "password-reset-failed";
    }

    // route used within our static/js/change-password.js
    // used as part of a fetch to our backend to change a users password
    // return  a string for the frontend to use

    @PostMapping("reset/new/password")
    public ResponseEntity<String> resetNewPassword(@RequestBody PasswordChangeRequest request){



        boolean passwordChanged = passwordService.updatePasswordWithToken(request);

        if(passwordChanged){
            return ResponseEntity.ok(frontEndDevUrl + "/auth/login");

        }
        return  ResponseEntity
                .internalServerError()
                .body(backEndDevUrl + "/password-reset-failed");

    }


}
