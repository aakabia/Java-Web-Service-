package com.example.demo.service;


import com.example.demo.exception.customExceptions.EmailSenderException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${backend.base-url}")
    private String backEndDevUrl;

    @Value("${frontend.base-url}")
    private String frontEndDevUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final JwtService jwtService;



    // sendEmail takes four arguments
    // creates a context for our email
    // uses the template and context to create content
    // sends email if everything is successful
    // throws EmailSenderException if any checked exception is thrown
    public void sendEmail(User user, String template, String subject, String errorMessage ) throws EmailSenderException {

        try{
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("BACK_END_DEV_URL", backEndDevUrl);
            context.setVariable("FRONT_END_DEV_URL", frontEndDevUrl);

            String htmlContent = templateEngine.process(template, context);

            MimeMessage message  = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);



        helper.setFrom(fromEmail);
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(htmlContent,true);


        mailSender.send(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new EmailSenderException(errorMessage);
        }


    }

    // resendEmail resends a Verification email for a user
    // only resends email if the users current token is expired
    // if token expired we replace the token and resend the email
    // throws EmailSenderException if any checked exception is thrown
    @Transactional(rollbackFor = Exception.class)
    public Optional<User> resendEmail(String opaqueToken) throws EmailSenderException {

        String template = "email-content.html";
        String subject = "Verification Email";
        String errorMessage = "Email Verification Could Not Send!";

        try{
            Optional<User> user = userRepository.findByOpaqueToken(opaqueToken);

            if(user.isEmpty()){
                return Optional.empty();
            }

            User userAccount = user.get();

            if( userAccount.isUserAccountEnabled() || jwtService.isTokenValid(userAccount.getVerificationToken(), userAccount)){
                throw new EmailSenderException(errorMessage);
            }

            var jwtVerificationToken = jwtService.generateVerificationToken(userAccount);

            userAccount.setVerificationToken(jwtVerificationToken);

            userRepository.save(userAccount);
            this.sendEmail(userAccount,template,subject,errorMessage);
            return user;


        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new EmailSenderException(errorMessage);
        }

    }


    // resendEmail resends a Password Reset email for a user
    // only resends email if the users current token is expired
    // if token expired we replace the token and resend the email
    // throws EmailSenderException if any checked exception is thrown
    @Transactional(rollbackFor = Exception.class)
    public Optional<User> resendPasswordEmail(String resetOpaqueToken) throws EmailSenderException {

        String template = "password-content.html";
        String subject = "Reset Password Email";
        String errorMessage = "Reset Password Email Could Not Send!";
        String resetCode = UUID.randomUUID().toString().replace("-","").substring(0,7);

        try{
            Optional<User> user = userRepository.findByResetOpaqueToken(resetOpaqueToken);

            if(user.isEmpty()){
                return Optional.empty();
            }

            User userAccount = user.get();

            if( jwtService.isTokenValid(userAccount.getResetVerificationToken(), userAccount)){
                throw new EmailSenderException(errorMessage);
            }

            var jwtVerificationToken = jwtService.generateVerificationToken(userAccount);

            userAccount.setResetVerificationToken(jwtVerificationToken);
            userAccount.setResetCode(resetCode);

            userRepository.save(userAccount);
            this.sendEmail(userAccount,template,subject,errorMessage);
            return user;


        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new EmailSenderException(errorMessage);
        }

    }





}
