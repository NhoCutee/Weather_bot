# 🤖 Spring Boot Telegram Bot (Weather & Inline Query)

Dự án Telegram Bot đa năng được xây dựng bằng **Java 17**, **Spring Boot** và thư viện **TelegramBots**. Bot hỗ trợ tra cứu dự báo thời tiết theo thời gian thực (qua Open-Meteo API) và cho phép người dùng gọi bot trực tiếp trong bất kỳ cuộc trò chuyện nào thông qua chế độ **Inline Mode**.

---

## 🌟 Tính Năng Nổi Bật

- 💬 **Tương tác trực tiếp:** Trò chuyện 1-1 hoặc trong Group qua các lệnh `/start`, `/help`.
- 🌤️ **Dự báo thời tiết:** Tra cứu nhiệt độ, độ ẩm, tốc độ gió, trạng thái thời tiết tại bất kỳ thành phố nào trên thế giới bằng lệnh `/weather <tên thành phố>`.
- ⚡ **Inline Query Mode:** Gõ `@your_bot_name <thành phố>` ngay trong khung chat với bạn bè để xem trước và gửi thông tin thời tiết nhanh chóng.
- ⚙️ **Kiến trúc phân lớp chuẩn:** Tách biệt rõ ràng giữa Controller/Bot, Service, Config và DTO/Model.
- 📝 **Ghi Log chuyên nghiệp:** Sử dụng **Lombok `@Slf4j`** để quản lý log dễ dàng và sạch sẽ.

---

## 📂 Cấu Trúc Package (Project Structure)

```text
src/main/java/com/example/telegram_bot/
 ├── bot/
 │    └── TelegramBot.java         # Tiếp nhận cập nhật từ Telegram (Chat message & Inline query)
 ├── config/
 │    └── TelegramConfig.java      # Cấu hình RestTemplate & Đăng ký Bot session với Telegram API
 ├── service/
 │    └── WeatherService.java      # Logic gọi Open-Meteo API, xử lý toạ độ và thời tiết
 ├── model/                        # Chứa các model / DTO (nếu mở rộng)
 └── TelegramBotApplication.java   # File khởi chạy chính của Spring Boot
```

---

## 🛠️ Công Nghệ Sử Dụng

- **Java:** 17
- **Framework:** Spring Boot (với `spring-boot-starter-web`)
- **Telegram Library:** `telegrambots-spring-boot-starter` (v6.9.7.1)
- **Tiện ích:** Lombok, Jackson Databind, RestTemplate, UriComponentsBuilder
- **API Thời tiết:** [Open-Meteo API](https://open-meteo.com/) (Miễn phí, không cần API Key)

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án

### 1. Chuẩn bị Bot trên Telegram
1. Mở Telegram và tìm bot **`@BotFather`**.
2. Gõ `/newbot` và làm theo hướng dẫn để tạo bot mới.
3. Sau khi tạo xong, `@BotFather` sẽ cấp cho bạn một chuỗi **Bot Token** và **Username**.
4. **Bật Inline Mode:**
   - Gửi lệnh `/mybots` ➡️ Chọn bot của bạn.
   - Chọn `Bot Settings` ➡️ `Inline Mode` ➡️ Bấm `Turn on`.
   - Chọn `Edit inline placeholder` ➡️ Nhập: `Nhập tên thành phố...`.

### 2. Cấu hình Dự án
Tạo file `src/main/resources/application.properties` (dựa trên file mẫu `application.properties.example`):

```properties
spring.application.name=Telegram_bot

# Điền Username và Token của bot bạn nhận từ @BotFather
bot.name=your_bot_username
bot.token=your_telegram_bot_token_here
```

### 3. Chạy Ứng Dụng
- **Chạy bằng IntelliJ IDEA / Eclipse:** Mở file `TelegramBotApplication.java` và chọn **Run**.
- **Chạy bằng Maven qua Terminal:**
  ```bash
  ./mvnw spring-boot:run
  ```

---

## 📖 Hướng Dẫn Sử Dụng

| Lệnh | Mô tả | Ví dụ |
| :--- | :--- | :--- |
| `/start` | Khởi động bot và hiển thị menu trợ giúp | `/start` |
| `/help` | Xem hướng dẫn sử dụng các lệnh | `/help` |
| `/weather <tên_thành_phố>` | Tra cứu thông tin thời tiết | `/weather Hanoi`<br>`/weather Saigon`<br>`/weather Tokyo` |
| `@<bot_username> <thành_phố>` | *(Inline)* Chia sẻ nhanh thời tiết trong mọi cuộc trò chuyện | `@meobonebot Danang` |

---
