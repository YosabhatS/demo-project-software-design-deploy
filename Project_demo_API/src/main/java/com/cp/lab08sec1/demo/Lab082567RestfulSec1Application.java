package com.cp.lab08sec1.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Configuration
@EnableJpaRepositories(basePackages = {
        "com.cp.lab08sec1.demo.repository",
        "com.cp.lab08sec1.demo.staff.repository"
})
public class Lab082567RestfulSec1Application {

	public static void main(String[] args) {
		SpringApplication.run(Lab082567RestfulSec1Application.class, args);
	}

}
