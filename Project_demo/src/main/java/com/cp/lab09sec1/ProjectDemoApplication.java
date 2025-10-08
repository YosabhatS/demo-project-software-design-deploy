package com.cp.lab09sec1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {
	    DataSourceAutoConfiguration.class, 
	    HibernateJpaAutoConfiguration.class
	})
@EnableConfigurationProperties(StaffApiProperties.class)
public class ProjectDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectDemoApplication.class, args);
	}

}
