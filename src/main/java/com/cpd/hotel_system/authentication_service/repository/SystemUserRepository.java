package com.cpd.hotel_system.authentication_service.repository;

import com.cpd.hotel_system.authentication_service.entity.SystemUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemUserRepository extends JpaRepository<SystemUserEntity, String> {
    public Optional<SystemUserEntity> findByEmail(String email);
}
