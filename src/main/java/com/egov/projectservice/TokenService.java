package com.egov.projectservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TokenService
{
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private ApplicationContext ctx;

    public Principal validateToken(String token)
    {

        log.info("TokenService.validateToken() called with token: " + token);

        WebClient authValidateWebClient = ctx.getBean("authValidateWebClient", WebClient.class);

        log.info("Calling auth-service to validate token: " + token);
        // forward a request to the auth service for validation
        Principal authResponse = authValidateWebClient.get()
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(Principal.class)
                .block(); // Thread is Blocked until the response is received | SYNC

        log.info("Response from auth-service: " + authResponse);

        if (authResponse.getState().equals("VALID"))
        {
            log.info("Token is valid");
            return authResponse;
        }
        else if (authResponse.getState().equals("INVALID"))
        {
            log.info("Token is invalid");
            return authResponse;
        }
        else
        {
            log.info("Error in auth-service: " + authResponse);
            throw new RuntimeException("Error in auth-service: " + authResponse);
        }

        // if the token is valid, return true
        // else return false
        //return null;
    }


}
