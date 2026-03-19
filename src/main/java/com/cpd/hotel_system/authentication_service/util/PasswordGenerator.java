package com.cpd.hotel_system.authentication_service.util;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class PasswordGenerator {
    public static final String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String lowerCase = "abcdefghijklmnopqrstuvwxyz";
    public static final String digits = "0123456789";
    public static final String specialCharacters = "!@#$%^&*(){}[]+=_-";
    public static final int passwordLength = 8;

    public static final String allChars = upperCase + lowerCase + digits + specialCharacters;

    public String generatePassword(){
        StringBuilder password = new StringBuilder(passwordLength);
        Random random = new Random();

        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        for (int i=4; i<passwordLength; i++){
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        return shuffleString(password.toString(), random);
    }

    private String shuffleString(String input, Random random) {
        char[] chars = input.toCharArray();
        for (int i=chars.length-1; i>0; i--){
            int k = random.nextInt(i+1);
            char temp = chars[i];
            chars[i] = chars[k];
            chars[k] = temp;
        }
        return new String(chars);
    }
}
