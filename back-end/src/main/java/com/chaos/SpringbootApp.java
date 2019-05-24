package com.chaos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@ServletComponentScan(basePackages = "com.chaos.web")
@MapperScan("com.chaos.mapper")
@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
public class SpringbootApp {
    public static void main(String args[]){
        SpringApplication springApplication=new SpringApplication(SpringbootApp.class);
        springApplication.setBannerMode(Banner.Mode.OFF);
        springApplication.run(args);
    }
}
