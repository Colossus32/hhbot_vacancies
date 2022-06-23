package com.colossus.hhbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    private  String id;
    private String name;
    private Salary salary;
    private String experience;
    private Employer employer;
}
