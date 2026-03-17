package com.cpd.hotel_system.authentication_service.repository;

import com.cpd.hotel_system.authentication_service.entity.OTPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTPEntity, String> {
    @Query(value = "SELECT * FROM otp WHERE system_user_id=?1", nativeQuery = true)
    public Optional<OTPEntity> findBySystemUserId(String systemUserId);
}
