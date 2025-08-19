package com.egov.matchservice;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProConLinkRepository extends MongoRepository<ProConLink, String> {

    ProConLink findByProjectid(String projectid);
}



