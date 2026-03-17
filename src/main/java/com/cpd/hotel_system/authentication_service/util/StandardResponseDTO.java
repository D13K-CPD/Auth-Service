package com.cpd.hotel_system.authentication_service.util;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponseDTO {
    private int statusCode;
    private String message;
    private Object data;
}
