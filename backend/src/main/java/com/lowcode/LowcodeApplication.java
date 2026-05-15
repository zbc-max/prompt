package com.lowcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lowcode")
public class LowcodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(LowcodeApplication.class, args);
    }
}
