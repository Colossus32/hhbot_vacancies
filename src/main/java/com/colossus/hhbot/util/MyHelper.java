package com.colossus.hhbot.util;

import com.colossus.hhbot.entity.GetFromHH;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MyHelper {
    public static Set<String>   getVacancies() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hh.ru/vacancies?text=java&area=19&experience=between1And3"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String body = response.body();

        GetFromHH fromOneToThree = mapper.readValue(body,GetFromHH.class); ///////////////

        request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hh.ru/vacancies?text=java&area=19&experience=noExperience"))
                .build();
        response = client.send(request,HttpResponse.BodyHandlers.ofString());
        body = response.body();

        GetFromHH noExperience = mapper.readValue(body,GetFromHH.class);

        Set<String> result = new HashSet<>();

        fromOneToThree.getItems().forEach(job -> result.add(job.getId()));
        noExperience.getItems().forEach(job -> result.add(job.getId()));

        return result;
    }

    public static Set<String> getVacanciesFromFile() throws IOException {
        Set<String> result = new HashSet<>();
        File file = new File("G:\\_JAVA\\hhbot\\ids.txt");
        if (file.exists()){
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String fromFile = reader.readLine();
            reader.close();
            String[] array = fromFile.trim().split(" ");
            Collections.addAll(result, array);
            return result;
        }
        else return new HashSet<>();
    }
}
