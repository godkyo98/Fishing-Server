package kyofishing.manager;

import kyoeconomy.api.KyoEconomyAPI;
import kyofishing.config.KyoFishingConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Random;

public class JackpotManager {
    private static final Random RANDOM = new Random();

    public static void roll(ServerPlayer player, ServerLevel level) {
        // Đổ xí ngầu từ 0.0 đến 100.0
        double roll = RANDOM.nextDouble() * 100;

        // KIỂM TRA TRÚNG TÚI TIỀN (Sử dụng config của bạn)
        if (roll <= KyoFishingConfig.data.jackpotMoneyChance) {

            // Lấy khoảng min/max từ Config của bạn
            long min = KyoFishingConfig.data.jackpotMoneyMin;
            long max = KyoFishingConfig.data.jackpotMoneyMax;

            // Random số tiền (Hào)
            long bonusMoney = min + (long)(RANDOM.nextDouble() * (max - min));

            // Gọi API KyoEconomy
            KyoEconomyAPI.addMoneyInHao(level.getServer(), player.getUUID(), bonusMoney);

            // Hiệu ứng rớt kinh nghiệm & Hạt Happy Villager
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 15, 0.5, 0.5, 0.5, 0.1);

            // Gửi thông báo
            player.sendSystemMessage(Component.literal("§6§l[JACKPOT] §aBùm! Bạn câu trúng một Túi Tiền trị giá §e" + bonusMoney + " Hào§a!"));
        }
    }
}