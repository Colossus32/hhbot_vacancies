package com.colossus.hhbot.entity;

import lombok.Data;

@Data
public class Job {

    private String id;
    private String name;
    private Salary salary;
    private String experience;
    private Employer employer;
}
