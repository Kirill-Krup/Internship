package com.internship.authservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "services.user-service")
public class UserServiceProperties {
    private String baseUrl;
    private String createPath;
    private String deletePath;
}