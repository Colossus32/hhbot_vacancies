package com.colossus.hhbot.entity;

import com.colossus.hhbot.util.MyHelper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Logger;

@Component
@Slf4j
public class JavaBot {

    TelegramBot bot;

    public JavaBot()  {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("G:\\_JAVA\\hhbot\\token.txt"));
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
                //System.out.println(chatId);
                String message = it.message().text();

                if (message != null)
                {
                    log.info("the message was not empty");
                    switch (message) {
                        case "/start":
                            botStart(chatId);
                            break;
                        case "/a":
                            botArchive();
                            break;
                        default:
                            //http client for each request
                            log.info("request 1-3 years experience requested vacancies");
                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create("https://api.hh.ru/vacancies?text=" + it.message().text() + "&area=19&experience=between1And3"))
                                    .build();

                            try {
                                //send request and get response, print in console what we got
                                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                                //json response body to Job class mapping
                                ObjectMapper mapper = new ObjectMapper();

                                //don't break, if we get a value for a field from our Job class, that we don't have
                                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                                //cast response body in the console. It helped me to understand a structure of response body's structure. Hide if needed.
                                String body = response.body();
                                System.out.println(body.substring(0,100));

                                //Mapper for our entity from response body
                                GetFromHH hh = mapper.readValue(body, GetFromHH.class);

                                //for empty result and not for first message
                                if (hh.getItems().size() < 1 && !it.message().text().equals("/start")) {
                                    log.info("nothing to show, list is empty");
                                    bot.execute(new SendMessage(chatId, "nothing was found"));
                                }

                                //cut items size to 7 if it's bigger
                                int quantityOfVacancies = 0;
                                if (hh.getItems().size() > 7) {
                                    quantityOfVacancies = hh.getItems().size() - 7;
                                    hh.setItems(hh.getItems().subList(0, 7));
                                }

                                hh.getItems().forEach(job -> {
                                    String salaryFrom, salaryTo;

                                    //handle with null in "salary"
                                    if (job.getSalary() == null) {
                                        salaryFrom = "undefined";
                                        salaryTo = "undefined";
                                    } else {
                                        //check for fields salary.from and salary.to in "salary"
                                        salaryFrom = job.getSalary().getFrom() == 0 ? "free" : String.valueOf(job.getSalary().getFrom());
                                        salaryTo = job.getSalary().getTo() == 0 ? "unlimited" : String.valueOf(job.getSalary().getTo());

                                    }
                                    //send a response form
                                    bot.execute(new SendMessage(chatId,
                                            job.getName() + " | " + job.getEmployer().getName() + " | " + salaryFrom + " - " + salaryTo +
                                                    //add link and easy // to hide it
                                                    "\n" + "https://hh.ru/vacancy/" + job.getId()
                                    ));
                                });
                                if (quantityOfVacancies > 0)
                                    bot.execute(new SendMessage(chatId, "and there are " + quantityOfVacancies + " more"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                    }
                }
            });
            // VV to the chat to say bot got all this
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    @Scheduled(cron = "${report.period}")
    //@Scheduled(cron = "${report.periodtest}")
    public void saveExistingVacationsToFile() throws IOException, InterruptedException {
        log.info("update existing vacancies every day");
        File file = new File("G:\\_JAVA\\hhbot\\ids.txt");
        if (file.exists()) file.delete();

        Set<String> set = MyHelper.getVacancies();

        if (set.size() > 0){
            BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
            set.forEach(s -> {
                try {
                    writer.write(s + " ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        }
    }

    @Scheduled(cron = "${report.ask}")
    //@Scheduled(cron = "${report.asktest}")
    public void checkAndSendNewVacancies() throws IOException, InterruptedException {
        log.info("check new vacancies");
        File file = new File("G:\\_JAVA\\hhbot\\ids.txt");
        if (!file.exists()) {
            saveExistingVacationsToFile();
            new HashSet<String>(Objects.requireNonNull(MyHelper.getVacanciesFromFile())).forEach(s -> {
                bot.execute(new SendMessage(1126891709, "https://hh.ru/vacancy/" + s));
            });
        }

        Set<String> setFromFile = new HashSet<String>(Objects.requireNonNull(MyHelper.getVacanciesFromFile()));

        Set<String> freshSet = new HashSet<>(MyHelper.getVacancies());

        freshSet.forEach(s -> {
            if (!setFromFile.contains(s)){
                bot.execute(new SendMessage(1126891709, "https://hh.ru/vacancy/" + s));
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
                    writer.write(s + " ");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void botStart(Long chatId){
        log.info("greetings");
        bot.execute(new SendMessage(chatId, "Welcome! Try to find a major!"));
    }
    private void botArchive(){
        log.info("request all archived vacancies");
        File file = new File("G:\\_JAVA\\hhbot\\ids.txt");
        if (file.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String fromFile;
                StringBuilder builder = new StringBuilder();
                while ((fromFile = reader.readLine()) != null){
                    builder.append(fromFile).append(" ");
                }
                String[] str = builder.toString().trim().split(" ");
                for (String s: str) {
                    bot.execute(new SendMessage(1126891709,"https://hh.ru/vacancy/"+ s ));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
