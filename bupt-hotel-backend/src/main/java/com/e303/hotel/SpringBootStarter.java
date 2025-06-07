package com.e303.hotel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan("com.e303.hotel.mapper")
@SpringBootApplication
public class SpringBootStarter {
    public static void main(String[] args) {
        ApplicationContext ioc = SpringApplication.run(SpringBootStarter.class, args);
        System.out.println(ioc);
    }
}
