package kyofishing.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KyoFishingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "kyofishing.json");

    // Biến lưu trữ dữ liệu config trên RAM
    public static ConfigData data = new ConfigData();

    public static class ConfigData {
        // Bảng giá: Tên Item -> Giá tiền (Hào)
        public Map<String, Long> fishPrices = new HashMap<>();
        // Danh sách rác cần hủy
        public List<String> trashItems = List.of(
                "minecraft:rotten_flesh", "minecraft:leather_boots",
                "minecraft:bone", "minecraft:stick", "minecraft:string",
                "minecraft:bowl", "minecraft:ink_sac", "minecraft:kelp"
        );

        // --- CẤU HÌNH JACKPOT MỚI ---
        public double jackpotMoneyChance = 5.0;    // 5% ra túi tiền
        public long jackpotMoneyMin = 100;      // Tối thiểu 100 Hào
        public long jackpotMoneyMax = 1000;      // Tối đa 1 Xu

        // Khởi tạo giá trị mặc định nếu file chưa tồn tại
        public ConfigData() {
            fishPrices.put("minecraft:cod", 1L);
            fishPrices.put("minecraft:salmon", 1L);
            fishPrices.put("minecraft:pufferfish", 1L);
            fishPrices.put("minecraft:tropical_fish", 1L);
        }
    }

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                FileReader reader = new FileReader(CONFIG_FILE);
                data = GSON.fromJson(reader, ConfigData.class);
                reader.close();
            } else {
                save(); // Tạo file mới
            }
            System.out.println("[KyoFishing] Đã nạp thành công Bảng Giá Cá!");
        } catch (Exception e) {
            System.err.println("[KyoFishing] Lỗi đọc config kyofishing.json!");
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(CONFIG_FILE);
            GSON.toJson(data, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}