package com.cpd.hotel_system.authentication_service.service;

import com.cpd.hotel_system.authentication_service.dto.request.SystemUserRequestDTO;

public interface SystemUserService {
    public void createSystemUser(SystemUserRequestDTO dto);
}
