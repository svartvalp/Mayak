package com.kasyan313.Mayak;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.kasyan313.Mayak"})
@Import(HibernateConfig.class)
public class AppConfig {

}
