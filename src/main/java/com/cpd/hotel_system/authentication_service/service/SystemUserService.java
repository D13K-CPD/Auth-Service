package com.cpd.hotel_system.authentication_service.service;

import com.cpd.hotel_system.authentication_service.dto.request.LoginRequestDTO;
import com.cpd.hotel_system.authentication_service.dto.request.PasswordResetRequestDTO;
import com.cpd.hotel_system.authentication_service.dto.request.SystemUserRequestDTO;
import java.io.IOException;
import java.util.List;

public interface SystemUserService {
    public void createSystemUser(SystemUserRequestDTO dto) throws IOException;
    public void initializeHost(List<SystemUserRequestDTO> users) throws IOException;
    public void resend(String email, String type);
    public void forgetPasswordSendVerificationCode(String email);
    public boolean verifyReset(String otp, String email);
    public boolean passwordReset(PasswordResetRequestDTO dto);
    public boolean verifyEmail(String otp, String email);
    public Object userLogin(LoginRequestDTO dto);
}
