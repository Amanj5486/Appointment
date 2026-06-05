package com.example.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {


    @JsonProperty("medications")
    List<Map<String, String>> medications;
    @JsonProperty("test")
    List<Map<String, String>> test ;
    @JsonProperty("followup_date")
    Long followupDate;
    String oxy ;
    String bp;
    String sugar;
    List<String> advice;
    List<String> diagnosis;
    String audioUrl;
    String imgUrl;
}
