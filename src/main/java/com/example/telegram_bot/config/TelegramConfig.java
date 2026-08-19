package com.example.telegram_bot.config;

import com.example.telegram_bot.bot.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            log.info("✅ Telegram Bot đã được đăng ký và sẵn sàng nhận tin nhắn!");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("❌ Lỗi khi đăng ký Telegram Bot: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
