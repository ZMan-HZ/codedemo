package com.zzm.demos.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 */
@SpringBootApplication(scanBasePackages = {"com.zzm.demos"})
@ImportResource({"classpath*:application-bean.xml"})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
