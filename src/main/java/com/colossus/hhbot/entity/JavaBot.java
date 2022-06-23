package com.colossus.hhbot.entity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class JavaBot {

    TelegramBot bot;

    public JavaBot() {
        this.bot = new TelegramBot("5479198567:AAGT2D76JV8KkGR8Z4Hv5qQ7kYQIyPX2Kvk");
    }

    public void listen(){

        bot.setUpdatesListener(element ->{
            element.forEach(it -> {
                //http client for each request
                HttpClient client = HttpClient.newHttpClient();
                //values from api.hh.ru java bryansk exp 1-3
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.hh.ru/vacancies?text=java&area=19&experience=between1And3"))
                        .build();
                try{
                    //send request and get response, print in console what we got
                    HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

                    //json response body to Job class mapping
                    ObjectMapper mapper = new ObjectMapper();
                    //don't break, if we get a value for a field from our Job class, that we have not
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String body = response.body();
                    System.out.println(body);
                    GetFromHH hh = mapper.readValue(body, GetFromHH.class);
                    hh.items.forEach(job -> {
                        int from,to;
                        if (job.getSalary() == null){
                            from = 0;
                            to = 0;
                        }else {
                            from = job.getSalary().getFrom();
                            to = job.getSalary().getTo();
                        }
                        bot.execute(new SendMessage(it.message().chat().id(), job.getName() + " | " + job.getEmployer().getName()+ " : " + from + " - " + to));// + "    " + job.getExperience() + "    " + job.getSalary()));
                        response.body();
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }
            });
            // VV to chat
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
