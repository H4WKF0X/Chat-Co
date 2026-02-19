package com.chatco.chatco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:secret.properties")
public class ChatCoApplication {

    @Value("${truststore.password}")
    private String truststorePassword;

    public static void main(String[] args) {
        SpringApplication.run(ChatCoApplication.class, args);
    }

    @PostConstruct
    public void init() {
        String truststorePath = System.getProperty("user.dir") + "/truststore.jks";
        System.setProperty("javax.net.ssl.trustStore", truststorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
    }
}