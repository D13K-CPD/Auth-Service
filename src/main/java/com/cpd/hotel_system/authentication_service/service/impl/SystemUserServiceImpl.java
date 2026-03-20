package com.cpd.hotel_system.authentication_service.service.impl;

import com.cpd.hotel_system.authentication_service.config.KeycloakSecurityUtil;
import com.cpd.hotel_system.authentication_service.dto.request.LoginRequestDTO;
import com.cpd.hotel_system.authentication_service.dto.request.PasswordResetRequestDTO;
import com.cpd.hotel_system.authentication_service.dto.request.SystemUserRequestDTO;
import com.cpd.hotel_system.authentication_service.entity.OTPEntity;
import com.cpd.hotel_system.authentication_service.entity.SystemUserEntity;
import com.cpd.hotel_system.authentication_service.exception.BadRequestException;
import com.cpd.hotel_system.authentication_service.exception.DuplicateEntryException;
import com.cpd.hotel_system.authentication_service.exception.EntryNotFoundException;
import com.cpd.hotel_system.authentication_service.repository.OTPRepository;
import com.cpd.hotel_system.authentication_service.repository.SystemUserRepository;
import com.cpd.hotel_system.authentication_service.service.EmailService;
import com.cpd.hotel_system.authentication_service.service.SystemUserService;
import com.cpd.hotel_system.authentication_service.util.OTPGenerator;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {
    @Value("${keycloak.config.realm}")
    private String realm;

    private final SystemUserRepository systemUserRepository;
    private final OTPRepository otpRepository;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final OTPGenerator otpGenerator;
    private final EmailService emailService;

    @Override
    public void createSystemUser(SystemUserRequestDTO dto) throws IOException {
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()){
            throw new BadRequestException("First name is required");
        }

        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()){
            throw new BadRequestException("Last name is required");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()){
            throw new BadRequestException("Email is required");
        }

        String systemUserId = "";
        String otp = "";
        Keycloak keycloak = null;

        UserRepresentation existingUser = null;
        keycloak = keycloakSecurityUtil.getKeycloakInstance();

        existingUser = keycloak.realm(realm).users().search(dto.getEmail()).stream().findFirst().orElse(null);

        if (existingUser != null){
            Optional<SystemUserEntity> selectedSystemUserFromAuthService =
                    systemUserRepository.findByEmail(dto.getEmail());

            if (selectedSystemUserFromAuthService.isEmpty()){
                keycloak.realm(realm).users().delete(existingUser.getId());
            } else {
                throw new DuplicateEntryException("Email already exist");
            }
        } else {
            Optional<SystemUserEntity> selectedSystemUserFromAuthService =
                    systemUserRepository.findByEmail(dto.getEmail());

            if (selectedSystemUserFromAuthService.isPresent()){
                Optional<OTPEntity> selectedOTP =
                        otpRepository.findBySystemUserId(selectedSystemUserFromAuthService.get().getUserId());

                selectedOTP.ifPresent(otpEntity -> otpRepository.deleteById(otpEntity.getOtpId()));

                systemUserRepository.deleteById(selectedSystemUserFromAuthService.get().getUserId());
            }
        }

        UserRepresentation userRepresentation = mapUserRepo(dto, false, false);
        Response response = keycloak.realm(realm).users().create(userRepresentation);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()){
            RoleRepresentation userRole = keycloak.realm(realm).roles().get("user").toRepresentation();
            systemUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(systemUserId).roles().realmLevel().add(Arrays.asList(userRole));
            UserRepresentation createdUser = keycloak.realm(realm).users().get(systemUserId).toRepresentation();

            SystemUserEntity systemUser = SystemUserEntity.builder()
                    .userId(systemUserId)
                    .keycloakId(createdUser.getId())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .email(dto.getEmail())
                    .contactNumber(dto.getContactNumber())
                    .isActive(false)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isEnabled(false)
                    .isEmailVerified(false)
                    .createdAt(new Date().toInstant())
                    .updatedAt(new Date().toInstant())
                    .build();
            SystemUserEntity savedUser = systemUserRepository.save(systemUser);

            OTPEntity createdOTP = OTPEntity.builder()
                    .otpId(UUID.randomUUID().toString())
                    .code(otpGenerator.generateOTP(6))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .isVerified(false)
                    .attempts(0)
                    .build();
            OTPEntity savedOTP = otpRepository.save(createdOTP);

            emailService.sendUserSignupVerificationCode(dto.getFirstName(), dto.getEmail(), "Verify Your Email", createdOTP.getCode());
        }
    }

    @Override
    public void initializeHost(List<SystemUserRequestDTO> users) throws IOException {
        for (SystemUserRequestDTO dto: users){
            Optional<SystemUserEntity> selectedUser = systemUserRepository.findByEmail(dto.getEmail());

            if (selectedUser.isPresent()){
                continue;
            }

            String systemUserId = "";
            String otp = "";
            Keycloak keycloak = null;

            UserRepresentation existingUser = null;
            keycloak = keycloakSecurityUtil.getKeycloakInstance();

            existingUser = keycloak.realm(realm).users().search(dto.getEmail()).stream().findFirst().orElse(null);

            if (existingUser != null){
                Optional<SystemUserEntity> selectedSystemUserFromAuthService =
                        systemUserRepository.findByEmail(dto.getEmail());

                if (selectedSystemUserFromAuthService.isEmpty()){
                    keycloak.realm(realm).users().delete(existingUser.getId());
                } else {
                    throw new DuplicateEntryException("Email already exist");
                }
            } else {
                Optional<SystemUserEntity> selectedSystemUserFromAuthService =
                        systemUserRepository.findByEmail(dto.getEmail());

                if (selectedSystemUserFromAuthService.isPresent()){
                    Optional<OTPEntity> selectedOTP =
                            otpRepository.findBySystemUserId(selectedSystemUserFromAuthService.get().getUserId());

                    selectedOTP.ifPresent(otpEntity -> otpRepository.deleteById(otpEntity.getOtpId()));

                    systemUserRepository.deleteById(selectedSystemUserFromAuthService.get().getUserId());
                }
            }

            UserRepresentation userRepresentation = mapUserRepo(dto, true, true);
            Response response = keycloak.realm(realm).users().create(userRepresentation);
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()){
                RoleRepresentation userRole = keycloak.realm(realm).roles().get("host").toRepresentation();
                systemUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                keycloak.realm(realm).users().get(systemUserId).roles().realmLevel().add(Arrays.asList(userRole));
                UserRepresentation createdUser = keycloak.realm(realm).users().get(systemUserId).toRepresentation();

                SystemUserEntity systemUser = SystemUserEntity.builder()
                        .userId(systemUserId)
                        .keycloakId(createdUser.getId())
                        .firstName(dto.getFirstName())
                        .lastName(dto.getLastName())
                        .email(dto.getEmail())
                        .contactNumber(dto.getContactNumber())
                        .isActive(true)
                        .isAccountNonExpired(true)
                        .isAccountNonLocked(true)
                        .isEnabled(true)
                        .isEmailVerified(true)
                        .createdAt(new Date().toInstant())
                        .updatedAt(new Date().toInstant())
                        .build();
                SystemUserEntity savedUser = systemUserRepository.save(systemUser);

                emailService.sendHostPassword(dto.getFirstName(), dto.getEmail(), "Access system by using above password", dto.getPassword());
            }
        }
    }

    private UserRepresentation mapUserRepo(SystemUserRequestDTO dto, boolean isEmailVerified, boolean isEnabled){
        UserRepresentation user = new UserRepresentation();

        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getEmail());
        user.setEnabled(isEnabled);
        user.setEmailVerified(isEmailVerified);
        List<CredentialRepresentation> credentialList = new ArrayList<>();
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setValue(dto.getPassword());
        credentialList.add(credentials);
        user.setCredentials(credentialList);
        return user;
    }

    @Override
    public void resend(String email, String type) {
        try{
            Optional<SystemUserEntity> selectedUser = systemUserRepository.findByEmail(email);

            if (selectedUser.isEmpty()){
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address");
            }

            SystemUserEntity systemUser = selectedUser.get();

            if (type.equalsIgnoreCase("SIGNUP") && systemUser.isEmailVerified()){
                throw new DuplicateEntryException("This email is already activated");
            }

            OTPEntity selectedOTP = systemUser.getOtp();
                String code = otpGenerator.generateOTP(6);
                emailService.sendUserSignupVerificationCode(systemUser.getFirstName(), systemUser.getEmail(), "Verify your email", code);
                selectedOTP.setAttempts(0);
                selectedOTP.setCode(code);
                selectedOTP.setVerified(false);
                selectedOTP.setUpdatedAt(new Date().toInstant());
                otpRepository.save(selectedOTP);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void forgetPasswordSendVerificationCode(String email) {
        try{
            Optional<SystemUserEntity> selectedUser = systemUserRepository.findByEmail(email);

            if (selectedUser.isEmpty()){
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address");
            }

            SystemUserEntity systemUser = selectedUser.get();
            Keycloak keycloak = null;
            keycloak = keycloakSecurityUtil.getKeycloakInstance();
            UserRepresentation existingUser = keycloak.realm(realm).users().search(email).stream().findFirst().orElse(null);

            if (existingUser == null){
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address");
            }

            OTPEntity selectedOTP = systemUser.getOtp();
            String code = otpGenerator.generateOTP(6);

            selectedOTP.setAttempts(0);
            selectedOTP.setCode(code);
            selectedOTP.setVerified(false);
            selectedOTP.setUpdatedAt(new Date().toInstant());
            otpRepository.save(selectedOTP);

            emailService.sendUserSignupVerificationCode(systemUser.getFirstName(), systemUser.getEmail(),
                    "Verify your email to reset the password", code);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean verifyReset(String otp, String email) {
        try{
            Optional<SystemUserEntity> selectedUser = systemUserRepository.findByEmail(email);

            if (selectedUser.isEmpty()){
                throw new EntryNotFoundException("Unable to find any users associated with the provided email address");
            }

            SystemUserEntity systemUser = selectedUser.get();
            OTPEntity otpObject = systemUser.getOtp();

            if (otpObject.getCode().equals(otp)){
                otpObject.setAttempts(otpObject.getAttempts() + 1);
                otpObject.setUpdatedAt(new Date().toInstant());
                otpObject.setVerified(true);
                otpRepository.save(otpObject);
                return true;
            } else {
                if (otpObject.getAttempts() >= 5){
                    resend(email, "PASSWORD");
                    throw new BadRequestException("You have a new verification code");
                }

                otpObject.setAttempts(otpObject.getAttempts() + 1);
                otpObject.setUpdatedAt(new Date().toInstant());
                otpRepository.save(otpObject);
                return false;
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean passwordReset(PasswordResetRequestDTO dto) {
        try{
            Optional<SystemUserEntity> selectedUser = systemUserRepository.findByEmail(dto.getEmail());

            if (selectedUser.isPresent()){
                SystemUserEntity systemUser = selectedUser.get();
                OTPEntity otpObject = systemUser.getOtp();
                Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
                List<UserRepresentation> keycloakUsers = keycloak.realm(realm).users().search(systemUser.getEmail());

                if (!keycloakUsers.isEmpty() && otpObject.getCode().equals(dto.getCode())){
                    UserRepresentation keycloakUser = keycloakUsers.get(0);
                    UserResource userResource = keycloak.realm(realm).users().get(keycloakUser.getId());
                    CredentialRepresentation newPassword = new CredentialRepresentation();
                    newPassword.setType(CredentialRepresentation.PASSWORD);
                    newPassword.setValue(dto.getPassword());
                    newPassword.setTemporary(false);
                    userResource.resetPassword(newPassword);

                    systemUser.setUpdatedAt(new Date().toInstant());
                    systemUserRepository.save(systemUser);

                    return true;
                }
                throw new BadRequestException("Try again");
            }
            throw new EntryNotFoundException("Unable to find");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean verifyEmail(String otp, String email) {
        Optional<SystemUserEntity> selectedUser = systemUserRepository.findByEmail(email);

        if (selectedUser.isEmpty()){
            throw new EntryNotFoundException("Can't find the associated user");
        }

        SystemUserEntity systemUser = selectedUser.get();
        OTPEntity otpObject = systemUser.getOtp();

        if (otpObject.isVerified()){
            throw new BadRequestException("This OTP has been used");
        }

        if (otpObject.getAttempts() >= 5){
            resend(email, "SIGNUP");
            return false;
        }

        if (otpObject.getCode().equals(otp)){
            UserRepresentation keycloakUser = keycloakSecurityUtil.getKeycloakInstance().realm(realm)
                    .users()
                    .search(email)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EntryNotFoundException("User not found"));

            keycloakUser.setEmailVerified(true);
            keycloakUser.setEnabled(true);

            keycloakSecurityUtil.getKeycloakInstance().realm(realm).users().get(keycloakUser.getId()).update(keycloakUser);

            systemUser.setEmailVerified(true);
            systemUser.setEnabled(true);
            systemUser.setActive(true);

            systemUserRepository.save(systemUser);

            otpObject.setVerified(true);
            otpObject.setAttempts(otpObject.getAttempts() + 1);

            otpRepository.save(otpObject);

            return true;
        } else {
            if (otpObject.getAttempts() >= 5){
                resend(email, "SIGNUP");
                return false;
            }

            otpObject.setAttempts(otpObject.getAttempts() + 1);
            otpRepository.save(otpObject);
        }
        return false;
    }

    @Override
    public Object userLogin(LoginRequestDTO dto) {
        Optional<SystemUserEntity> selectedUser = systemUserRepository.findByEmail(dto.getEmail());
        if (selectedUser.isEmpty()){
            throw new EntryNotFoundException("Can't find the associated user");
        }

        SystemUserEntity systemUser = selectedUser.get();
        if (!systemUser.isEmailVerified()){
            resend(dto.getEmail(), "SIGNUP");
            throw new UnsupportedOperationException("Please verify email");
        }

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", "");
        requestBody.add("grant_type", OAuth2Constants.PASSWORD);
        requestBody.add("username", dto.getEmail());
        requestBody.add("client_secret", "");
        requestBody.add("password", dto.getPassword());

        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Objects> response = restTemplate.postForEntity("keycloak api url", requestBody, Objects.class);
        return response.getBody();
    }
}
