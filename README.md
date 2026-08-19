# 🤖 Telegram Weather & Inline Bot (TypeScript + grammY)

Bot Telegram đa năng được viết hoàn toàn bằng **TypeScript** và framework **grammY** hiện đại. Siêu nhẹ (tiêu tốn **< 30MB RAM**), khởi động tức thì trong **0.1s**, hỗ trợ tra cứu thời tiết thời gian thực và chế độ **Inline Query** chia sẻ nhanh trong mọi cuộc trò chuyện.

---

## 🌟 Tính Năng Nổi Bật

- ⚡ **Siêu nhẹ & Tốc độ cao:** Sử dụng **grammY** (framework TypeScript #1 cho Telegram).
- 🌤️ **Dự báo thời tiết:** Tra cứu nhiệt độ, độ ẩm, tốc độ gió theo thời gian thực qua Open-Meteo API.
- 🚀 **Inline Mode:** Gõ `@your_bot_name <thành_phố>` trong bất kỳ khung chat nào để gửi thông tin thời tiết trực tiếp.
- 💾 **In-Memory Caching:** Lưu bộ nhớ đệm 10 phút, phản hồi kết quả gần như tức thì (**0ms**).
- 🔒 **Bảo mật:** Quản lý token an toàn qua `.env`.

---

## 📂 Cấu Trúc Thư Mục (Project Structure)

```text
src/
 ├── config.ts              # Đọc cấu hình biến môi trường (.env)
 ├── services/
 │    └── weather.service.ts # Logic gọi API Open-Meteo + In-memory Cache
 └── bot.ts                 # Điểm khởi chạy Bot (Chat Commands & Inline Query)
├── .env.example           # File mẫu biến môi trường
├── package.json           # Khai báo thư viện & scripts
├── tsconfig.json          # Cấu hình trình biên dịch TypeScript
└── Dockerfile             # Multi-stage Docker build siêu nhẹ
```

---

## 🛠️ Cài Đặt & Chạy Dự Án

### 1. Yêu cầu
* **Node.js:** Phiên bản 18+ hoặc 20+
* **npm** hoặc **yarn / pnpm**

### 2. Cài đặt thư viện
```bash
npm install
```

### 3. Cấu hình biến môi trường
Tạo file `.env` từ file mẫu `.env.example`:
```env
BOT_TOKEN=your_telegram_bot_token_here
```

### 4. Khởi chạy Bot

* **Môi trường phát triển (Tự reload khi sửa code):**
  ```bash
  npm run dev
  ```

* **Môi trường Production:**
  ```bash
  npm run build
  npm start
  ```

---

## 📖 Danh Sách Lệnh

| Lệnh | Mô tả | Ví dụ |
| :--- | :--- | :--- |
| `/start` | Khởi động bot và xem danh sách lệnh | `/start` |
| `/help` | Xem hướng dẫn sử dụng | `/help` |
| `/weather <tên_thành_phố>` | Tra cứu dự báo thời tiết | `/weather Hanoi`<br>`/weather Saigon`<br>`/weather Tokyo` |
| `@<bot_username> <thành_phố>` | *(Inline)* Chia sẻ nhanh thời tiết trong mọi cuộc trò chuyện | `@meobonebot Danang` |
