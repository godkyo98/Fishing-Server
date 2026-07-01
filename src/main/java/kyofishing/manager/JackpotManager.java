package kyofishing.manager;

import kyoeconomy.api.KyoEconomyAPI;
import kyofishing.config.KyoFishingConfig;
import kyofishing.data.KyoFishingState;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Random;

public class JackpotManager {
    private static final Random RANDOM = new Random();

    public static void roll(ServerPlayer player, ServerLevel level) {
        // --- FIX 26.2: Dùng lookupOrThrow và getOrThrow theo cheuẩn HolderLookup mới ---
        Holder<Enchantment> luckOfSeaHolder = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.LUCK_OF_THE_SEA);

        // 1. LẤY CẤP ĐỘ PHÙ PHÉP "MAY MẮN CỦA BIỂN CẢ"
        ItemStack mainHand = player.getMainHandItem();
        int luckLevel = EnchantmentHelper.getItemEnchantmentLevel(luckOfSeaHolder, mainHand);

        if (luckLevel == 0) {
            ItemStack offHand = player.getOffhandItem();
            luckLevel = EnchantmentHelper.getItemEnchantmentLevel(luckOfSeaHolder, offHand);
        }

        // 2. TÍNH TOÁN TỶ LỆ NỔ HŨ (Cơ bản + Bonus từ bùa)
        double baseChance = KyoFishingConfig.data.jackpotMoneyChance;
        double bonusChance = luckLevel * 2.0;
        double finalChance = baseChance + bonusChance;

        double roll = RANDOM.nextDouble() * 100;

        // 3. KIỂM TRA TRÚNG TÚI TIỀN
        if (roll <= finalChance) {

            // --- TÍNH NĂNG V2: SCALE TIỀN THEO LEVEL ---
            KyoFishingState state = KyoFishingState.getServerState(level.getServer());
            int fishingLevel = state.getFishingLevel(player.getUUID());

            // Tính Hệ số mốc (Milestone Multiplier)
            long milestoneMultiplier = Math.max(1, fishingLevel / 10);

            // Nhân hệ số mốc vào giá trị Min/Max từ Config
            long min = KyoFishingConfig.data.jackpotMoneyMin * milestoneMultiplier;
            long max = KyoFishingConfig.data.jackpotMoneyMax * milestoneMultiplier;

            // Random số tiền (Hào) dựa trên mốc mới
            long bonusMoney = min + (long)(RANDOM.nextDouble() * (max - min));

            // Thưởng thêm từ Bùa (Được nhân theo mốc)
            if (luckLevel > 0) {
                bonusMoney += (luckLevel * 50L * milestoneMultiplier);
            }

            // Gọi API KyoEconomy nạp tiền
            KyoEconomyAPI.addMoneyInHao(level.getServer(), player.getUUID(), bonusMoney);

            // Hiệu ứng rớt kinh nghiệm & Hạt Happy Villager
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 15, 0.5, 0.5, 0.5, 0.1);

            // Gắn thêm Tiền tố Vinh danh mốc Level
            String milestoneTag = fishingLevel >= 10 ? "§b[Mốc Lv." + (fishingLevel / 10 * 10) + "] " : "";

            if (luckLevel > 0) {
                player.sendSystemMessage(Component.literal("§6§l[JACKPOT] " + milestoneTag + "§aNhờ bùa May Mắn, bạn trúng §e" + bonusMoney + " Hào§a!"));
            } else {
                player.sendSystemMessage(Component.literal("§6§l[JACKPOT] " + milestoneTag + "§aBùm! Bạn câu trúng Túi Tiền trị giá §e" + bonusMoney + " Hào§a!"));
            }
        }
    }
}