package com.colossus.hhbot.entity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class JavaBot {

    TelegramBot bot;

    public JavaBot()  {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("token.txt"));
            String path = reader.readLine();
            this.bot = new TelegramBot(path);
        } catch (IOException e){
            System.out.println("bot initialization error :/ ");
            e.printStackTrace();
        }
    }

    public void listen(){

        //get every element to listen and do our work
        bot.setUpdatesListener(element ->{
            element.forEach(it -> {
                //for often using
                Long chatId = it.message().chat().id();

                if (it.message().text().equals("/start")){
                    bot.execute(new SendMessage(chatId, "Welcome! Try to find a major!"));
                }

                //http client for each request
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.hh.ru/vacancies?text=" + it.message().text() + "&area=19&experience=between1And3"))
                        .build();

                try{
                    //send request and get response, print in console what we got
                    HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

                    //json response body to Job class mapping
                    ObjectMapper mapper = new ObjectMapper();

                    //don't break, if we get a value for a field from our Job class, that we don't have
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    //cast response body in the console. It helped me to understand a structure of response body's structure. Hide if needed.
                    String body = response.body();
                    System.out.println(body);

                    //Mapper for our entity from response body
                    GetFromHH hh = mapper.readValue(body, GetFromHH.class);

                    //for empty result and not for first message
                    if (hh.items.size() < 1 && !it.message().text().equals("/start")) {
                        bot.execute(new SendMessage(chatId, "nothing was found"));
                    }

                    //cut items size to 7 if it's bigger
                    int quantityOfVacancies = 0;
                    if (hh.items.size()> 7) {
                        quantityOfVacancies = hh.items.size() - 7;
                        hh.items = hh.items.subList(0,7);
                    }

                    hh.items.forEach(job -> {
                        String salaryFrom, salaryTo;

                        //handle with null in "salary"
                        if (job.getSalary() == null){
                            salaryFrom = "undefined";
                            salaryTo = "undefined";
                        }else {
                            //check for fields salary.from and salary.to in "salary"
                            salaryFrom = job.getSalary().getFrom() == 0 ? "free" : String.valueOf(job.getSalary().getFrom());
                            salaryTo = job.getSalary().getTo() == 0 ? "unlimited" : String.valueOf(job.getSalary().getTo());

                        }
                        //send a response form
                        bot.execute(new SendMessage(chatId,
                                job.getName() + " | " + job.getEmployer().getName()+ " | " + salaryFrom + " - " + salaryTo +
                                        //add link and easy // to hide it
                                        "\n" + "https://hh.ru/vacancy/" + job.getId()
                        ));
                    });
                    if(quantityOfVacancies > 0) bot.execute(new SendMessage(chatId, "and there are " + quantityOfVacancies + " more"));

                } catch (Exception e){
                    e.printStackTrace();
                }
            });
            // VV to the chat to say bot got all this
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
