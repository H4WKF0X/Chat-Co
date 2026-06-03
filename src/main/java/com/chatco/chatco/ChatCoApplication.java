package com.chatco.chatco;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:secret.properties", ignoreResourceNotFound = true)
public class ChatCoApplication {

    @Value("${truststore.password:}")
    private String truststorePassword;

    public static void main(String[] args) {
        SpringApplication.run(ChatCoApplication.class, args);
    }

    @PostConstruct
    public void init() {
        if (!truststorePassword.isBlank()) {
            String truststorePath = System.getProperty("user.dir") + "/truststore.jks";
            System.setProperty("javax.net.ssl.trustStore", truststorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
        }
    }
}
