package com.colossus.hhbot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetFromHH {
    //items because of hh api documentation

    //private JsonNode items;
    @JsonProperty("items")
    List<Job> items;
}
