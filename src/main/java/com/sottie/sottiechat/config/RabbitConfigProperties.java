package com.sottie.sottiechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.rabbitmq")
public class RabbitConfigProperties {
    private String hostName;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
}
