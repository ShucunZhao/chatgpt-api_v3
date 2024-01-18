package com.prj.chatgpt; //Define the package of the class.

// Import the necessary classes and interfaces for the class.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @SpringBootApplication: This annotation is a convenience for adding all the typical configurations needed to start a Spring application.
 * It encompasses features like @Configuration, @EnableAutoConfiguration, @ComponentScan, etc.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Entry point of the Java program. It starts the Spring application.
        SpringApplication.run(Application.class, args);
    }
}