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

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${backend.base-url}")
    private String backEndDevUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final JwtService jwtService;




    public void sendEmail(User user, String template, String subject, String errorMessage ) throws EmailSenderException {

        try{
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("BACK_END_DEV_URL", backEndDevUrl);

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





}
