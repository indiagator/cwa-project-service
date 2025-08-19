package com.egov.projectservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("api/v1")
public class MainRestController
{
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    private ApplicationContext ctx;

    @PostMapping("float/project")
    public ResponseEntity<?> floatProject(@RequestBody Project project,
                                          @RequestHeader("Authorization") String token)
    { // AOP - Aspected Oriented Programming - Micrometer does its job here [ trace + span generation]
        logger.info("Request received to float project: " + project);
        Principal principal =  tokenService.validateToken(token);
        if(principal.getState().equals("VALID"))
        {
            logger.info("Token validated successfully");
            // Token is valid, proceed with the update
            if(project.getOwnerPhone().equals(principal.getUsername())) // AUTHORIZATION OF REQUEST HAPPENS HERE
            {
                logger.info("Phone number matches with the token");
                project.setStatus("FLOATED");
                projectRepository.save(project);
                logger.info("Project floated successfully in the database: " + project);
                // Forward an ASYNC Request to the Comm-Service to send messages to the Contractors
                WebClient commServiceWebClient = ctx.getBean("commServiceWebClient", WebClient.class);

                commServiceWebClient.post()
                        .header("Authorization", token)
                        .bodyValue(project)
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribe(response -> logger.info("Notification sent to contractors: " + response),
                                error -> logger.error("Error sending notification to contractors: " + error.getMessage()));

                return  ResponseEntity.ok("Project Floated Successfully");
            }
            else
            {
                logger.info("Phone number does not match with the token");
                return ResponseEntity.status(401).body("Unauthorized: Phone number does not match with the token");
            }
        }
        else
        {
            logger.info("Token not valid");
            return ResponseEntity.status(401).body("Unauthorized: Invalid Token");
        }
    }

}
