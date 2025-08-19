package com.egov.matchservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1")
public class MainRestController
{
    @Autowired
    ProConLinkRepository proConLinkRepository;
    @Autowired
    TokenService tokenService;

    @PostMapping("/proconlink")
    public ResponseEntity<?> connectProjectContractors(@RequestBody ProConLink proConLink,
                                                       @RequestHeader("Authorization") String token)
    {
        if(tokenService.validateToken(token))
        {
            proConLinkRepository.save(proConLink);
            return ResponseEntity.ok("Project Contractor Links Saved");
        }
        else
            return ResponseEntity.status(401).body("Invalid token");
    }

    @GetMapping("/proconlink/{projectid}")
    public ResponseEntity<?> getProConLinksForProject(@PathVariable("projectid") String projectid)
    {
        ProConLink proConLink =  proConLinkRepository.findByProjectid(projectid);
        return ResponseEntity.ok(proConLink);
    }


}
