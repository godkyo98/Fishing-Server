package kyofishing.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.HashMap;
import java.util.UUID;

public class KyoFishingState extends SavedData {

    // Lưu trữ số lượng item đã câu được của từng người chơi
    public final HashMap<UUID, Integer> fishCaught = new HashMap<>();

    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    // Codec chuẩn Minecraft 26.2 để đọc/ghi file tự động
    public static final Codec<KyoFishingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUID_CODEC, Codec.INT).fieldOf("fish_caught").forGetter(state -> state.fishCaught)
    ).apply(instance, map -> {
        KyoFishingState state = new KyoFishingState();
        state.fishCaught.putAll(map);
        return state;
    }));

    private static final SavedDataType<KyoFishingState> TYPE = new SavedDataType<>(
            Identifier.parse("kyofishing:stats"), // Lưu thành kyofishing/stats.dat
            KyoFishingState::new,
            CODEC,
            null
    );

    public static KyoFishingState getServerState(MinecraftServer server) {
        SavedDataStorage dataStorage = server.overworld().getDataStorage();
        return dataStorage.computeIfAbsent(TYPE);
    }

    // --- CÁC HÀM TIỆN ÍCH ---
    public int getFishCount(UUID uuid) {
        return fishCaught.getOrDefault(uuid, 0);
    }

    public void addFish(UUID uuid, int amount) {
        fishCaught.put(uuid, getFishCount(uuid) + amount);
        this.setDirty(); // Báo cho server biết cần lưu file này lại
    }

    public int getFishingLevel(UUID uuid) {
        int count = getFishCount(uuid);

        // Nếu câu dưới 50 cá thì luôn là Level 1
        if (count < 50) {
            return 1;
        }
        // Bắt đầu tính Level từ con cá thứ 50 trở đi.
        // Trừ đi 50 cá của cấp đầu tiên để làm mốc tính căn bậc 2 cho các cấp sau.
        // Công thức: Cấp độ = 1 + căn bậc hai của ((Số cá - 50) / hệ_số_khó)

        // Ở đây mình chọn hệ số khó là 30.0 để đường cong kinh nghiệm đẹp hơn:
        int level = 1 + (int) Math.sqrt((count - 50) / 30.0) + 1;

        return level;
    }
}