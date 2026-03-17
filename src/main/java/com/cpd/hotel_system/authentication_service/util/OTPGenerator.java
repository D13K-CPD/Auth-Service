package com.cpd.hotel_system.authentication_service.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OTPGenerator {
    public String generateOTP(int length){
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();

        for (int i=0; i<length; i++){
            sb.append(random.nextInt(10));
        }

        while (sb.charAt(0) == '0'){
            sb.setCharAt(0, (char) ('1'+ random.nextInt(9)));
        }

        return sb.toString();
    }
}
