package com.gmm.bot;

import com.gmm.bot.ai.BaseBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class BotApplication {

    private final BaseBot bot;

    public BotApplication(BaseBot bot) {
        this.bot = bot;
    }

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runBot(){
        bot.start();
    }

}
