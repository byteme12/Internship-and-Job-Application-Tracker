package com.usj.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrackerApplication {
    public static void main(String[] args) {
        // Works around a TLS 1.3 handshake failure ("fatal alert: internal_error")
        // between recent JDKs and MongoDB Atlas by forcing TLS 1.2.
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
        SpringApplication.run(TrackerApplication.class, args);
    }
}