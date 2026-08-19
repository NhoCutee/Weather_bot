import axios from "axios";

interface WeatherCache {
  data: string;
  timestamp: number;
}

export class WeatherService {
  private cache = new Map<string, WeatherCache>();
  private readonly CACHE_DURATION_MS = 10 * 60 * 1000; // 10 phút

  public async getWeather(cityName: string): Promise<string> {
    const key = cityName.trim().toLowerCase();

    // 1. Kiểm tra nếu có trong Cache -> Trả về tức thì (0ms)
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.CACHE_DURATION_MS) {
      return cached.data;
    }

    try {
      // 2. Tìm toạ độ thành phố qua Open-Meteo Geocoding
      const geoUrl = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(
        cityName.trim()
      )}&count=1&language=vi&format=json`;

      const geoRes = await axios.get(geoUrl, {
        headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" },
        timeout: 5000,
      });

      if (!geoRes.data?.results || geoRes.data.results.length === 0) {
        return `❌ Không tìm thấy thông tin cho địa điểm: *${cityName}*. Vui lòng kiểm tra lại tên thành phố!`;
      }

      const location = geoRes.data.results[0];
      const { latitude, longitude, name: foundCity, country = "" } = location;

      // 3. Lấy dữ liệu thời tiết theo toạ độ
      const weatherUrl = `https://api.open-meteo.com/v1/forecast?latitude=${latitude}&longitude=${longitude}&current_weather=true`;
      const weatherRes = await axios.get(weatherUrl, {
        headers: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" },
        timeout: 5000,
      });

      const current = weatherRes.data?.current_weather;
      if (!current) {
        return "⚠️ Hiện tại không lấy được dữ liệu thời tiết cho khu vực này.";
      }

      const temp: number = current.temperature;
      const windspeed: number = current.windspeed;
      const weatherCode: number = current.weathercode;
      const weatherDescription = this.decodeWeatherCode(weatherCode);

      const result = `🌤 *THÔNG TIN THỜI TIẾT*
📍 *Địa điểm:* ${foundCity.toUpperCase()} ${country ? `(${country})` : ""}
🌡️ *Nhiệt độ:* ${temp.toFixed(1)}°C
🌥️ *Trạng thái:* ${weatherDescription}
💨 *Tốc độ gió:* ${windspeed.toFixed(1)} km/h`;

      // Lưu Cache 10 phút
      this.cache.set(key, { data: result, timestamp: Date.now() });
      return result;
    } catch (error: any) {
      return `❌ Đã có lỗi xảy ra khi lấy dữ liệu: ${error.message}`;
    }
  }

  private decodeWeatherCode(code: number): string {
    switch (code) {
      case 0:
        return "Trời quang đãng, nắng đẹp ☀️";
      case 1:
      case 2:
      case 3:
        return "Trời nhiều mây rải rác ⛅";
      case 45:
      case 48:
        return "Có sương mù 🌫️";
      case 51:
      case 53:
      case 55:
        return "Mưa phùn nhỏ 🌦️";
      case 61:
      case 63:
      case 65:
        return "Mưa rào 🌧️";
      case 80:
      case 81:
      case 82:
        return "Mưa rào nặng hạt từng cơn ⛈️";
      case 95:
      case 96:
      case 99:
        return "Có dông bão sấm chớp ⚡";
      default:
        return "Thời tiết ổn định 🌤️";
    }
  }
}

export const weatherService = new WeatherService();
