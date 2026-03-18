package com.cpd.hotel_system.authentication_service.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class EmailTemplateHandler {
    public String loadHTMLTemplate(String templateName){
        try{
            ClassPathResource resource = new ClassPathResource(templateName);
            byte[] fileData = resource.getInputStream().readAllBytes();
            return new String(fileData, StandardCharsets.UTF_8);
        } catch (IOException e){
            e.printStackTrace();
            return "";
        }
    }
}
