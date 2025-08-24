package com.example.demo.auth.authController;

import com.example.demo.auth.authService.VerificationService;
import com.example.demo.exception.customExceptions.EmailSenderException;
import com.example.demo.model.User;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class VerificationController {


    private final VerificationService verificationService;
    private final EmailService emailService;
    @Value("${frontend.base-url}")
    private String frontEndDevUrl;


    // very user route that is used within email template "email-content.html"

    @GetMapping("/verify")
    public String verifyToken(@RequestParam(required = true, name = "token", defaultValue = "NONE") String token, Model model){

        Optional<String> message = verificationService.verifyUserAccount(token);

        if(message.isEmpty() || token.equals("NONE")){
            return "redirect:" + frontEndDevUrl + "/auth/register";
        }

        String verificationMessage = message.get();

         return verificationService.verificationResponse( model,verificationMessage,token);


    }

    // resend verification email route that is used within email template " verification-email-expired.html"

    @GetMapping("/verify/resend-email")
    public String resendEmail(@RequestParam(required = true, name = "token", defaultValue = "NONE") String token, Model model) throws EmailSenderException {

        Optional<User> user = emailService.resendEmail(token);

        if(user.isEmpty() || token.equals("NONE")){
            return "redirect:" + frontEndDevUrl + "/auth/register";
        }

        User userToVerify = user.get();

        model.addAttribute("user", userToVerify );
        model.addAttribute("FRONT_END_DEV_URL", frontEndDevUrl );

        return "resend-success";

    }




}
