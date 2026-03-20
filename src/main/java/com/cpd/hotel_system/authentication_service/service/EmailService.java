package com.cpd.hotel_system.authentication_service.service;

import java.io.IOException;

public interface EmailService {
    public boolean sendUserSignupVerificationCode(String firstName, String toEmail, String subject, String otp) throws IOException;
    public boolean sendHostPassword(String firstName, String toEmail, String subject, String password) throws IOException;
}
