package com.internship.authservice.config;

import com.internship.authservice.config.properties.UserServiceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UserServiceProperties.class)
public class PropertiesConfig {
}
