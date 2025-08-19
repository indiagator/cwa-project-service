package com.egov.projectservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class ProjectServiceApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }

}
