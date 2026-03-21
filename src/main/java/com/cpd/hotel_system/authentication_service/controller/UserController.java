package com.cpd.hotel_system.authentication_service.controller;

import com.cpd.hotel_system.authentication_service.config.JWTService;
import com.cpd.hotel_system.authentication_service.dto.request.LoginRequestDTO;
import com.cpd.hotel_system.authentication_service.dto.request.PasswordResetRequestDTO;
import com.cpd.hotel_system.authentication_service.dto.request.SystemUserRequestDTO;
import com.cpd.hotel_system.authentication_service.service.SystemUserService;
import com.cpd.hotel_system.authentication_service.util.StandardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/user-service/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final SystemUserService systemUserService;
    private final JWTService jwtService;

    @PostMapping("/visitors/signup")
    public ResponseEntity<StandardResponseDTO> create(
            @RequestBody SystemUserRequestDTO dto) throws IOException{
        systemUserService.createSystemUser(dto);
        return new ResponseEntity<>(
                new StandardResponseDTO(
                        201,
                        "User account was created",
                        null
                ), HttpStatus.CREATED
        );
    }

    @PostMapping("/visitors/resend")
    public ResponseEntity<StandardResponseDTO> resend(
            @RequestParam String email,
            @RequestParam String type) throws IOException{
            systemUserService.resend(email, type);
        return new ResponseEntity<>(
                new StandardResponseDTO(
                        200,
                        "Resend successful, Please Check your email.",
                        null
                ), HttpStatus.OK
        );
    }

    @PostMapping("/visitors/forget-password-verification-code")
    public ResponseEntity<StandardResponseDTO> forgetPasswordVerificationCode(
            @RequestParam String email) throws IOException{
        systemUserService.forgetPasswordSendVerificationCode(email);
        return new ResponseEntity<>(
                new StandardResponseDTO(
                        200,
                        "Code send successful, Please Check your email.",
                        null
                ), HttpStatus.OK
        );
    }

    @PostMapping("/visitors/verify-reset")
    public ResponseEntity<StandardResponseDTO> verifyReset(
            @RequestParam String otp,
            @RequestParam String email) throws IOException{
        boolean isVerified = systemUserService.verifyReset(otp, email);
        return new ResponseEntity<>(
                new StandardResponseDTO(
                        isVerified ? 200 : 400,
                        isVerified ? "Verified" : "Try again",
                        isVerified
                ), isVerified ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/visitors/password-reset")
    public ResponseEntity<StandardResponseDTO> passwordReset(
            @RequestBody PasswordResetRequestDTO dto) throws IOException{
        boolean isChanged = systemUserService.passwordReset(dto);
        return new ResponseEntity<>(
                new StandardResponseDTO(
                        isChanged ? 201 : 400,
                        isChanged ? "Password changed" : "Try again",
                        isChanged
                ), isChanged ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/visitors/verify-email")
    public ResponseEntity<StandardResponseDTO> verifyEmail(
            @RequestParam String otp,
            @RequestParam String email) throws IOException{
        boolean isVerified = systemUserService.verifyEmail(otp, email);
        return new ResponseEntity<>(
                new StandardResponseDTO(
                        isVerified ? 200 : 400,
                        isVerified ? "Verified" : "Try again",
                        isVerified
                ), isVerified ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/visitors/login")
    public ResponseEntity<StandardResponseDTO> login(
            @RequestBody LoginRequestDTO dto) throws IOException{
        return new ResponseEntity<>(
                new StandardResponseDTO(
                        200,
                        "Login success",
                        systemUserService.userLogin(dto)
                ), HttpStatus.OK
        );
    }
}
