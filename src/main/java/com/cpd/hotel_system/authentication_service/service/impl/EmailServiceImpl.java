package com.cpd.hotel_system.authentication_service.service.impl;

import com.cpd.hotel_system.authentication_service.service.EmailService;
import com.cpd.hotel_system.authentication_service.util.EmailTemplateHandler;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final EmailTemplateHandler emailTemplateHandler;

    @Value("${fromEmail}")
    private String senderEmail;

    @Value("${emailkey}")
    private String apiKey;

    @Override
    public boolean sendUserSignupVerificationCode(String firstName, String toEmail, String subject, String otp) throws IOException {
        String htmlBody = emailTemplateHandler.loadHTMLTemplate("templates/otpverification.html");

        htmlBody = htmlBody.replace("${firstName}", firstName);
        htmlBody = htmlBody.replace("${otp}", otp);
        htmlBody = htmlBody.replace("${year}", String.valueOf(Year.now()));

        Email from = new Email(senderEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }

        return true;
    }

    @Override
    public boolean sendHostPassword(String firstName, String toEmail, String subject, String password) throws IOException {
        String htmlBody = emailTemplateHandler.loadHTMLTemplate("templates/host-init.html");

        htmlBody = htmlBody.replace("${firstName}", firstName);
        htmlBody = htmlBody.replace("${password}", password);
        htmlBody = htmlBody.replace("${year}", String.valueOf(Year.now()));

        Email from = new Email(senderEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }

        return true;
    }
}
