import { Bot, InlineQueryResultBuilder } from "grammy";
import { config } from "./config.js";
import { weatherService } from "./services/weather.service.js";

// Khởi tạo bot bằng thư viện grammY
const bot = new Bot(config.botToken);

// ================= 1. XỬ LÝ LỆNH CHAT =================

// Lệnh /start
bot.command("start", async (ctx) => {
  await ctx.reply(
    `👋 Chào mừng bạn! Tôi là Bot hỗ trợ cá nhân (TypeScript Edition).

📌 *Danh sách lệnh:*
👉 \`/weather <tên thành phố>\`: Tra cứu thời tiết (VD: \`/weather Hanoi\`)
👉 Gõ \`@${ctx.me.username} Hanoi\` ở bất kỳ cuộc trò chuyện nào để chia sẻ nhanh!
👉 \`/help\`: Xem hướng dẫn sử dụng.`,
    { parse_mode: "Markdown" }
  );
});

// Lệnh /help
bot.command("help", async (ctx) => {
  await ctx.reply(
    `📖 *HƯỚNG DẪN:*
- Để xem thời tiết, hãy gõ \`/weather\` kèm theo tên thành phố (VD: \`/weather Hanoi\`, \`/weather Danang\`).
- *Inline Mode:* Gõ \`@${ctx.me.username} Hanoi\` khi đang nhắn với bạn bè để gửi thời tiết trực tiếp!`,
    { parse_mode: "Markdown" }
  );
});

// Lệnh /weather <tên_thành_phố>
bot.command("weather", async (ctx) => {
  const cityName = ctx.match?.trim();

  if (!cityName) {
    return ctx.reply("⚠️ Bạn chưa nhập tên thành phố!\n*Ví dụ đúng:* `/weather Hanoi`", {
      parse_mode: "Markdown",
    });
  }

  // Thông báo tạm thời hoặc lấy kết quả ngay
  const weatherInfo = await weatherService.getWeather(cityName);
  await ctx.reply(weatherInfo, { parse_mode: "Markdown" });
});

// ================= 2. XỬ LÝ INLINE QUERY (@meobonebot <thành_phố>) =================

bot.on("inline_query", async (ctx) => {
  const query = ctx.inlineQuery.query.trim();
  const results = [];

  if (!query) {
    // Khi người dùng chưa gõ tên thành phố: Hiện sẵn 3 thành phố lớn
    const [hn, dn, sg] = await Promise.all([
      weatherService.getWeather("Hanoi"),
      weatherService.getWeather("Danang"),
      weatherService.getWeather("Saigon"),
    ]);

    results.push(
      InlineQueryResultBuilder.article("1", "🌤 Thời tiết tại Hà Nội", {
        description: "Bấm để gửi dự báo thời tiết Hà Nội",
      }).text(hn, { parse_mode: "Markdown" }),

      InlineQueryResultBuilder.article("2", "🌤 Thời tiết tại Đà Nẵng", {
        description: "Bấm để gửi dự báo thời tiết Đà Nẵng",
      }).text(dn, { parse_mode: "Markdown" }),

      InlineQueryResultBuilder.article("3", "🌤 Thời tiết tại TP. Hồ Chí Minh", {
        description: "Bấm để gửi dự báo thời tiết TP.HCM",
      }).text(sg, { parse_mode: "Markdown" })
    );
  } else {
    // Khi người dùng nhập tên thành phố cụ thể
    const weatherInfo = await weatherService.getWeather(query);
    results.push(
      InlineQueryResultBuilder.article(`w_${query}`, `🌤 Thời tiết tại ${query.toUpperCase()}`, {
        description: `Bấm để gửi dự báo thời tiết ${query}`,
      }).text(weatherInfo, { parse_mode: "Markdown" })
    );
  }

  // Trả về kết quả và cache 300 giây trên client để phản hồi tức thì
  await ctx.answerInlineQuery(results, {
    cache_time: 300,
    is_personal: false,
  });
});

// Phản hồi tin nhắn văn bản thông thường
bot.on("message:text", async (ctx) => {
  await ctx.reply("🤖 Mình chưa hiểu lệnh này. Hãy thử gõ `/weather Hanoi` hoặc `/help` nhé!", {
    parse_mode: "Markdown",
  });
});

// Bắt lỗi toàn cục
bot.catch((err) => {
  console.error(`❌ Lỗi Bot:`, err.error);
});

// ================= 3. KHỞI CHẠY BOT (LONG POLLING) =================
bot.start({
  onStart: (botInfo) => {
    console.log(`===========================================`);
    console.log(`✅ Bot @${botInfo.username} đã chạy thành công bằng TypeScript!`);
    console.log(`⚡ Sẵn sàng nhận tin nhắn và Inline Query 24/7`);
    console.log(`===========================================`);
  },
});
