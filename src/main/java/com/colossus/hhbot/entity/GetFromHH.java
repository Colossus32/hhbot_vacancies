package com.colossus.hhbot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetFromHH {
    //items from api response
    @JsonProperty("items")
    List<Job> items;
}
