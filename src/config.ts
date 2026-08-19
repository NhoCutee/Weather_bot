import dotenv from "dotenv";

dotenv.config();

if (!process.env.BOT_TOKEN) {
  throw new Error("❌ Thiếu biến môi trường BOT_TOKEN trong file .env!");
}

export const config = {
  botToken: process.env.BOT_TOKEN,
};
