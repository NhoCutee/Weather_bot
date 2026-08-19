package com.example.telegram_bot.bot;

import com.example.telegram_bot.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String botName;
    private final WeatherService weatherService;

    public TelegramBot(@Value("${bot.name}") String botName,
                       @Value("${bot.token}") String botToken,
                       WeatherService weatherService) {
        super(botToken);
        this.botName = botName;
        this.weatherService = weatherService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // 1. Xử lý khi người dùng chat trực tiếp với bot (hoặc trong group)
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleChatMessage(update.getMessage());
        }
        // 2. Xử lý khi người dùng gọi bot bằng Inline (@meobonebot <thành phố>) ở bất kỳ đâu
        else if (update.hasInlineQuery()) {
            handleInlineQuery(update.getInlineQuery());
        }
    }

    // Xử lý tin nhắn chat thông thường
    private void handleChatMessage(Message message) {
        var chatId = message.getChatId();
        String text = message.getText().trim();
        log.info("Nhận tin nhắn từ [{}]: {}", chatId, text);

        if (text.equalsIgnoreCase("/start")) {
            sendTextMessage(chatId,
                    """
                            👋 Chào mừng bạn! Tôi là Bot hỗ trợ cá nhân.
                            📌 *Danh sách lệnh:*
                            👉 `/weather <tên thành phố>`: Tra cứu thời tiết (VD: `/weather Hanoi`)
                            👉 Gõ `@meobonebot Hanoi` ở bất kỳ cuộc trò chuyện nào để chia sẻ nhanh!
                            👉 `/help`: Xem hướng dẫn sử dụng.""");
        } else if (text.equalsIgnoreCase("/help")) {
            sendTextMessage(chatId,
                    """
                            📖 *HƯỚNG DẪN:*
                            - Để xem thời tiết, hãy gõ `/weather` kèm theo tên thành phố (VD: `/weather Hanoi`).
                            - *Inline Mode:* Gõ `@meobonebot Hanoi` khi đang nhắn với bạn bè để gửi thời tiết trực tiếp!""");
        } else if (text.startsWith("/weather")) {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2 || parts[1].isBlank()) {
                sendTextMessage(chatId, "⚠️ Bạn chưa nhập tên thành phố!\n*Ví dụ đúng:* `/weather Hanoi`");
            } else {
                String cityName = parts[1].trim();
                String weatherInfo = weatherService.getWeather(cityName);
                sendTextMessage(chatId, weatherInfo);
            }
        } else {
            sendTextMessage(chatId, "🤖 Mình chưa hiểu lệnh này. Hãy thử gõ `/weather Hanoi` hoặc `/help` nhé!");
        }
    }

    // Xử lý Inline Query khi gõ @meobonebot ở bất kỳ cuộc trò chuyện nào
    private void handleInlineQuery(InlineQuery inlineQuery) {
        String query = inlineQuery.getQuery().trim();
        String queryId = inlineQuery.getId();
        log.info("Nhận Inline Query từ user [{}]: '{}'", inlineQuery.getFrom().getId(), query);

        List<InlineQueryResult> results = new ArrayList<>();

        // Nếu người dùng chưa gõ gì hoặc gõ từ khoá
        if (query.isEmpty()) {
            // Gợi ý sẵn 3 thành phố lớn
            results.add(createWeatherArticle("1", "Hà Nội", weatherService.getWeather("Hanoi")));
            results.add(createWeatherArticle("2", "Đà Nẵng", weatherService.getWeather("Danang")));
            results.add(createWeatherArticle("3", "TP. Hồ Chí Minh", weatherService.getWeather("Saigon")));
        } else {
            // Lấy thời tiết theo từ khoá thành phố người dùng vừa gõ
            String weatherInfo = weatherService.getWeather(query);
            results.add(createWeatherArticle("1", query.toUpperCase(), weatherInfo));
        }

        AnswerInlineQuery answer = new AnswerInlineQuery();
        answer.setInlineQueryId(queryId);
        answer.setResults(results);
        answer.setCacheTime(10); // Lưu cache 10 giây để phản hồi nhanh

        try {
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Lỗi khi trả về lời phản hồi Inline Query: {}", e.getMessage());
        }
    }

    // Hàm phụ trợ tạo card kết quả Inline
    private InlineQueryResultArticle createWeatherArticle(String id, String cityName, String weatherContent) {
        InlineQueryResultArticle article = new InlineQueryResultArticle();
        article.setId(id);
        article.setTitle("🌤 Thời tiết tại " + cityName);
        article.setDescription("Bấm để gửi thông tin dự báo thời tiết");

        InputTextMessageContent messageContent = new InputTextMessageContent();
        messageContent.setMessageText(weatherContent);
        messageContent.setParseMode("Markdown");

        article.setInputMessageContent(messageContent);
        return article;
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Lỗi khi gửi tin nhắn Telegram: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }
}
