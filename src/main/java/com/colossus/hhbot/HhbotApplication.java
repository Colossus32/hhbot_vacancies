package com.colossus.hhbot;

import com.colossus.hhbot.entity.JavaBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HhbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(HhbotApplication.class, args);
        new JavaBot().listen();
    }

}
