package com.egov.projectservice;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("float/project")
    public ResponseEntity<?> floatProject(@RequestBody Project project,
                                          @RequestHeader("Authorization") String token,
                                          HttpServletResponse httpServletResponse,
                                          HttpServletRequest httpServletRequest)
    {
        Cookie[] cookies =  httpServletRequest.getCookies();
        List<Cookie> cookieList = new ArrayList<>();
        if(cookies != null && cookies.length > 0)
        {
            cookieList = Arrays.asList(cookies);
        }


       if(cookieList.stream().noneMatch(cookie -> cookie.getName().equals("float_project_stage_1")))
        {
            // NO RELEVANT COOKIE FOUND - SO PROCEEDING WITH FRESH REQUEST LOGIC

            // AOP - Aspected Oriented Programming - Micrometer does its job here [ trace + span generation]
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

                    String stage1Key = String.valueOf(new Random().nextInt(100000));
                   redisTemplate.opsForValue().set(stage1Key, "STAGE 1 COMPLETE | STAGE 2 IN PROGRESS", 30); // Store in Redis for 30 seconds

                    Cookie cookie = new Cookie("float_project_stage_1", stage1Key);


                    WebClient commServiceWebClient = ctx.getBean("commServiceWebClient", WebClient.class);

                    commServiceWebClient.post()
                            .header("Authorization", token)
                            .bodyValue(project)
                            .retrieve()
                            .bodyToMono(String.class)
                            .subscribe(

                                    response ->
                                    {
                                        logger.info("Notification sent to contractors: " + response);
                                        // Add more logic here if needed
                                        redisTemplate.opsForValue().set(stage1Key, "STAGE 1 COMPLETE | STAGE 2 COMPLETE | TRANSACTION COMPLETE", 30);
                                    },



                                    error -> logger.error("Error sending notification to contractors: " + error.getMessage())


                                      );



                    httpServletResponse.addCookie(cookie);
                    return  ResponseEntity.ok("/float/project | STAGE 1 COMPLETE | STAGE 2 IN PROGRESS");
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
       else
       {
           // RELEVANT COOKIE FOUND - SO PROCEEDING WITH FOLLOW UP LOGIC

           String cacheValue = (String) redisTemplate.opsForValue().get(cookieList.stream()
                   .filter(cookie -> cookie.getName().equals("float_project_stage_1"))
                   .findFirst()
                   .orElseThrow(() -> new RuntimeException("Cookie not found")).getValue());

           Cookie cookie = new Cookie("float_project_stage_2", "the new redis key for stage 2");

           return ResponseEntity.ok(cacheValue);
       }


    }

}
