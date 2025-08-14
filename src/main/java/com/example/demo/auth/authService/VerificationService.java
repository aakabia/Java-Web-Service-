package com.example.demo.auth.authService;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {


    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Value("${frontend.base-url}")
    private String frontEndDevUrl;



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
