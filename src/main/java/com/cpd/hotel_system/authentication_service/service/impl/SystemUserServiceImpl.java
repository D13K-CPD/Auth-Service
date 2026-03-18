package com.cpd.hotel_system.authentication_service.service.impl;

import com.cpd.hotel_system.authentication_service.config.KeycloakSecurityUtil;
import com.cpd.hotel_system.authentication_service.dto.request.SystemUserRequestDTO;
import com.cpd.hotel_system.authentication_service.entity.OTPEntity;
import com.cpd.hotel_system.authentication_service.entity.SystemUserEntity;
import com.cpd.hotel_system.authentication_service.exception.BadRequestException;
import com.cpd.hotel_system.authentication_service.exception.DuplicateEntryException;
import com.cpd.hotel_system.authentication_service.repository.OTPRepository;
import com.cpd.hotel_system.authentication_service.repository.SystemUserRepository;
import com.cpd.hotel_system.authentication_service.service.SystemUserService;
import com.cpd.hotel_system.authentication_service.util.OTPGenerator;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @Override
    public void createSystemUser(SystemUserRequestDTO dto) {
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

        UserRepresentation userRepresentation = mapUserRepo(dto);
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
        }
    }

    private UserRepresentation mapUserRepo(SystemUserRequestDTO dto){
        UserRepresentation user = new UserRepresentation();

        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getEmail());
        user.setEnabled(false);
        user.setEmailVerified(false);
        List<CredentialRepresentation> credentialList = new ArrayList<>();
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setValue(dto.getPassword());
        credentialList.add(credentials);
        user.setCredentials(credentialList);
        return user;
    }
}
