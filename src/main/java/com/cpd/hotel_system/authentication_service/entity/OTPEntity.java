package com.cpd.hotel_system.authentication_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "otp")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OTPEntity {

    @Id
    @Column(name = "otp_id", length = 80, nullable = false)
    private String otpId;

    @Column(name = "code", length = 80, nullable = false)
    private String code;

    @Column(name = "is_verified")
    private boolean isVerified;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "attempts")
    private Integer attempts;

    @OneToOne
    @JoinColumn(name = "system_user_id", nullable = false, unique = true)
    private SystemUserEntity systemUser;
}
