package com.egov.projectservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "projects")
@Getter
@Setter
public class Project
{
    @Id
    String id;
    String ownerPhone; // Phone number of the project owner
    String name;
    String description;
    String location;
    String startDate;
    String status; // PLANNED, IN_PROGRESS, COMPLETED, ON_HOLD
    double budget; // Estimated budget for the project
    List<String> messages; // List of messages or comments related to the quote
}
