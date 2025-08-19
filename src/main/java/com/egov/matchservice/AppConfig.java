package com.egov.matchservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class AppConfig
{
    @Autowired
    EurekaDiscoveryClient discoveryClient;

    @Bean
    @Scope("prototype")
    public WebClient authValidateWebClient(WebClient.Builder webClientBuilder)
    {
        List<ServiceInstance> instances = discoveryClient.getInstances("auth-service");
        //No load balancing algorithm is used here, so we are just taking the first instance
        // you can use load balancing algorithm like round robin or random if you want
        String hostname = instances.get(0).getHost();
        String port = String.valueOf(instances.get(0).getPort());

        return webClientBuilder
                .baseUrl(String.format("http://%s:%s/api/v1/validate", hostname, port))
                .filter(new LoggingWebClientFilter())
                .build();
    }


}
