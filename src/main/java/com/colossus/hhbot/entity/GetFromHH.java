package com.colossus.hhbot.entity;

import lombok.Data;

import java.util.List;

@Data
public class GetFromHH {
    //items because of hh api documentation

    //private JsonNode items;
    List<Job> items;
}
