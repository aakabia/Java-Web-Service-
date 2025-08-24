package com.example.demo.auth.authService;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {


    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Value("${frontend.base-url}")
    private String frontEndDevUrl;


    // verifyUserAccount is used to verify a users account once registered
    // we find the user by their opaque token so no jwts are exposed
    // if the verification token is used we return verified or if expired we return expired
    // if neither we update the opaque token, account enabled and verification token fields and save the user
    // last return that the user is now verified.

    @Transactional(rollbackFor = Exception.class)
    public Optional<String> verifyUserAccount(String opaqueToken){

        try{
        Optional<User> user = userRepository.findByOpaqueToken(opaqueToken);

        if(user.isEmpty()){
            return Optional.empty();
        }

        User userAccount = user.get();

        if(userAccount.getVerificationToken().equals("USED")){
            return Optional.of("USER VERIFIED!");
        } else if(!jwtService.isTokenValid(userAccount.getVerificationToken(), userAccount)){
            return Optional.of("TOKEN EXPIRED!");
        }

        userAccount.setVerificationToken("USED");
        userAccount.setOpaqueToken("USED");
        userAccount.setUserAccountEnabled(true);
        userRepository.save(userAccount);

        return  Optional.of("USER VERIFIED!");


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    // verificationResponse is responsible for returning a string that will be used to display a template
    // we find a user by their associated token
    // the template used is dependent on the value of message
    // since we use redirect: or templates like (verification-email-expired)  here this method will be used in a controller with @Controller not @RestController
    // reason being is the @Controller does not automatically have the request body in the response
    // method used in Verification Controller

    public String verificationResponse ( Model model, String message, String token){


        if(message.equals("TOKEN EXPIRED!")){
            Optional<User> user = userRepository.findByOpaqueToken(token);

            if(user.isPresent()){
                User userToVerify = user.get();
                model.addAttribute("user", userToVerify);
                return "verification-email-expired";

            }

        }


        return "redirect:" + frontEndDevUrl + "/auth/login";


    }

}
