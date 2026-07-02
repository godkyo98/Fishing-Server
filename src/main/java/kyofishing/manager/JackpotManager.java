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
import net.minecraft.sounds.SoundEvent;
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

            // --- SCALE TIỀN THEO LEVEL ---
            KyoFishingState state = KyoFishingState.getServerState(level.getServer());
            int fishingLevel = state.getFishingLevel(player.getUUID());

            long milestoneMultiplier = Math.max(1, fishingLevel / 10);

            long min = KyoFishingConfig.data.jackpotMoneyMin * milestoneMultiplier;
            long max = KyoFishingConfig.data.jackpotMoneyMax * milestoneMultiplier;

            long bonusMoney = min + (long)(RANDOM.nextDouble() * (max - min));

            if (luckLevel > 0) {
                bonusMoney += (luckLevel * 50L * milestoneMultiplier);
            }

            // Gọi API KyoEconomy nạp tiền
            KyoEconomyAPI.addMoneyInHao(level.getServer(), player.getUUID(), bonusMoney);

            // Hiệu ứng hạt hoành tráng tại vị trí người câu trúng hũ
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 25, 0.5, 0.5, 0.5, 0.1);

            // Lấy danh hiệu động và format tiền Xu/Hào
            String titleTag = getFishingTitleTag(fishingLevel);
            String displayMoney = formatMoneyDisplay(bonusMoney);

            // [HIỂN THỊ CÁ NHÂN]: Chat + Actionbar của riêng người chơi
            if (luckLevel > 0) {
                player.sendSystemMessage(Component.literal("§6§l[JACKPOT] " + titleTag + "§aNhờ bùa May Mắn, bạn trúng §e" + displayMoney + "§a!"));
            } else {
                player.sendSystemMessage(Component.literal("§6§l[JACKPOT] " + titleTag + "§aBùm! Bạn câu trúng Túi Tiền trị giá §e" + displayMoney + "§a!"));
            }
            player.sendOverlayMessage(Component.literal("§6§l★ JACKPOT NỔ HŨ: §e§l+" + displayMoney + " §6§l★"));

            // 🌟 TÍNH NĂNG ĐỈNH CAO V2 PRO: PHÂN TẦNG BROADCAST TOÀN SERVER 🌟
            if (fishingLevel >= 50) {
                broadcastToAllServer(level, player, fishingLevel, titleTag, displayMoney);
            }
        }
    }

    // Hệ thống thông báo và âm thanh phân cấp dựa theo mốc Level thực tế
    private static void broadcastToAllServer(ServerLevel level, ServerPlayer player, int fishingLevel, String titleTag, String displayMoney) {
        String border;
        String announceMessage;
        SoundEvent serverSound;
        float volume = 0.3f;
        float pitch = 1.0f;

        if (fishingLevel >= 3000) {
            // MỐC CHÚA TỂ: Đỏ sậm, viền đầu lâu nguy hiểm, sound Wither gầm
            border = "§4§l☠ ▬▬▬▬============ TỐI CAO VINH DANH ============▬▬▬ ☠";
            announceMessage = "§4§l[THẦN THOẠI CLAN] Thần câu " + titleTag + "§f§l" + player.getScoreboardName() + " §4§lđã giật sập hũ Jackpot câu trúng siêu giải thưởng §e§l" + displayMoney + "§4§l!!!";
            serverSound = SoundEvents.WITHER_DEATH;
            volume = 0.2f; // Sound lớn nên giảm volume tránh chói tai
        }
        else if (fishingLevel >= 2000) {
            // MỐC BẬC THẦY: Đỏ rực, viền sấm sét, sound Rồng gầm
            border = "§c§l⚡ ▬▬▬▬============ BẬC THẦY XUẤT THẾ ============▬▬▬ ⚡";
            announceMessage = "§c§l[CHẤN ĐỘNG] Đại cao thủ " + titleTag + "§f§l" + player.getScoreboardName() + " §cnổ hũ Jackpot nhận ngay một lượng của cải khổng lồ §e§l" + displayMoney + "§c§l!";
            serverSound = SoundEvents.ENDER_DRAGON_GROWL;
            volume = 0.25f;
        }
        else if (fishingLevel >= 1000) {
            // MỐC CHUYÊN GIA: Màu cam quý tộc, viền sao lấp lánh, sound Thử thách hoàn thành
            border = "§6§l★ 🌟 ★ 🌟 ★ 🌟 ★ KHÔNG TƯỞNG ★ 🌟 ★ 🌟 ★ 🌟 ★";
            announceMessage = "§6§l[ĐẲNG CẤP] Chuyên gia sát cá " + titleTag + "§f§l" + player.getScoreboardName() + " §6vừa câu trúng rương vàng Jackpot trị giá §e§l" + displayMoney + "§6!";
            serverSound = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
        }
        else if (fishingLevel >= 500) {
            // MỐC TINH ANH: Màu vàng, sound Cấp độ tăng lên thanh thoát
            border = "§e⚡ ⚡ ⚡ ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬ ⚡ ⚡ ⚡";
            announceMessage = "§e§l[TINH ANH] Cần thủ tài ba " + titleTag + "§f§l" + player.getScoreboardName() + " §emay mắn nổ hũ thu về §6§l" + displayMoney + "§e!";
            serverSound = SoundEvents.PLAYER_LEVELUP;
            pitch = 0.8f;
        }
        else if (fishingLevel >= 200) {
            // MỐC THỢ SĂN: Xanh lơ huyền ảo
            border = "§b✨ ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬ ✨";
            announceMessage = "§b§l[THỢ SĂN] Thợ săn biển sâu " + titleTag + "§f" + player.getScoreboardName() + " §bgiật được túi tiền Jackpot §e" + displayMoney + "§b!";
            serverSound = SoundEvents.PLAYER_LEVELUP;
            pitch = 1.2f;
        }
        else if (fishingLevel >= 100) {
            // MỐC TẬP SỰ: Xanh lá cây tươi, sound Tiếng đe keng vừa phải
            border = "§a=====================================================";
            announceMessage = "§a[TẬP SỰ] Cần thủ tập sự " + titleTag + "§f" + player.getScoreboardName() + " §anhận lộc Jackpot câu được §e" + displayMoney + "§a!";
            serverSound = SoundEvents.ANVIL_LAND;
            pitch = 1.1f;
        }
        else {
            // MỐC KHỞI ĐẦU (50 -> 99): Xanh lá đậm, sound Tiếng đe keng cao vút
            border = "§2-----------------------------------------------------";
            announceMessage = "§2[KHỞI ĐẦU] Thành viên " + titleTag + "§f" + player.getScoreboardName() + " §2vừa kích hoạt lộc Jackpot được §e" + displayMoney + "§2!";
            serverSound = SoundEvents.ANVIL_LAND;
            pitch = 1.5f;
        }

        // Thực hiện phát sóng và chạy âm thanh đến TẤT CẢ người chơi đang online trên Server
        for (ServerPlayer onlinePlayer : level.getServer().getPlayerList().getPlayers()) {
            onlinePlayer.sendSystemMessage(Component.literal(border));
            onlinePlayer.sendSystemMessage(Component.literal(announceMessage));
            onlinePlayer.sendSystemMessage(Component.literal(border));

            // Phát sound hiệu ứng tương ứng với mốc danh hiệu
            onlinePlayer.level().playSound(null, onlinePlayer.blockPosition(), serverSound, SoundSource.RECORDS, volume, pitch);
        }
    }

    // Hàm phân giải danh hiệu động dựa theo mã nguồn KyoFishing.java + Mốc cấp 50 Khởi Đầu
    private static String getFishingTitleTag(int level) {
        if (level >= 3000) {
            return "§4§l[Chúa Tể] ";
        } else if (level >= 2000) {
            return "§c[Bậc Thầy] ";
        } else if (level >= 1000) {
            return "§6[Chuyên Gia] ";
        } else if (level >= 500) {
            return "§e[Tinh Anh] ";
        } else if (level >= 200) {
            return "§b[Thợ Săn] ";
        } else if (level >= 100) {
            return "§a[Tập Sự] ";
        } else if (level >= 50) {
            return "§2[Khởi Đầu] ";
        }
        return "§7[Tân Thủ] ";
    }

    private static String formatMoneyDisplay(long totalHao) {
        long xu = totalHao / 1000;
        long hao = totalHao % 1000;

        if (xu > 0 && hao > 0) {
            return xu + " Xu " + hao + " Hào";
        } else if (xu > 0) {
            return xu + " Xu";
        } else {
            return hao + " Hào";
        }
    }
}