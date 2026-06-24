package com.quiz.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.quiz.app.mapper")
public class QuizAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuizAppApplication.class, args);
    }
}
