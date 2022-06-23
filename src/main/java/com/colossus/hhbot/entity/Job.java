package com.colossus.hhbot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Job {

    @JsonProperty("vacancy")
    private String vacancy;
    @JsonProperty("salary")
    private String salary;
    @JsonProperty("experience")
    private String experience;
}
