package com.example.EmailService.configs;

import com.example.EmailService.dtos.SendEmailDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;
import javax.mail.Session;

@Service
public class SendEmailService {

    private ObjectMapper objectMapper;
    private EmailUtil emailUtil;

    @Value("${mailId}")
    private String mailId;

    @Value("${password}")
    private String password;

    public SendEmailService(ObjectMapper objectMapper, EmailUtil emailUtil){
        this.objectMapper = objectMapper;
        this.emailUtil = emailUtil;

    }


    @KafkaListener(topics="sendEmail", groupId = "emailservice")
    public void sendEmail(String message){
        //Code to send email to user
        SendEmailDto sendEmailDto = null;

        try{
            sendEmailDto = objectMapper.readValue(message,SendEmailDto.class);
        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailId, password);
            }
        };
        Session session = Session.getInstance(props, auth);

        emailUtil.sendEmail(session,
                sendEmailDto.getTo(),
                sendEmailDto.getSubject(),
                sendEmailDto.getBody()
                );
    }
}
