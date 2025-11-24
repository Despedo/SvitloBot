package com.svitlobot.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@TestConfiguration
public class TelegramBotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi() {
        return Mockito.mock(TelegramBotsApi.class);
    }
}
