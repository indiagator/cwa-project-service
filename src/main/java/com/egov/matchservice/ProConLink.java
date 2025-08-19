package com.egov.matchservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(collection = "proconlinks")
@Getter
@Setter
public class ProConLink
{
    String projectid;
    List<Long> contractorids;
}
