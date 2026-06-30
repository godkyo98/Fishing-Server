package kyofishing.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;

import kyofishing.manager.AutoSellManager;
import kyofishing.manager.JackpotManager;
import kyofishing.config.KyoFishingConfig;
import kyoeconomy.api.KyoEconomyAPI;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Shadow
    public abstract Player getPlayerOwner();

    @Redirect(
            method = "retrieve",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean kyo$interceptFishLoot(Level level, Entity entity) {
        // 1. Nếu không phải là item rớt ra (VD: cục kinh nghiệm), cho rớt bình thường
        if (!(entity instanceof ItemEntity itemEntity)) {
            return level.addFreshEntity(entity);
        }

        Player player = this.getPlayerOwner();
        if (player == null) return level.addFreshEntity(entity);

        // 2. KÍCH HOẠT VÒNG QUAY NHÂN PHẨM VÀ CỘNG ĐIỂM LEVEL CÂU CÁ
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Đổ xí ngầu Jackpot
            JackpotManager.roll(serverPlayer, (ServerLevel) level);

            // CỘNG 1 ĐIỂM VÀO TỔNG SỐ CÁ CÂU ĐƯỢC
            kyofishing.data.KyoFishingState state = kyofishing.data.KyoFishingState.getServerState(serverPlayer.level().getServer());
            state.addFish(serverPlayer.getUUID(), 1);
        }

        // 3. KIỂM TRA CHẾ ĐỘ AUTO-SELL CỦA NGƯỜI CHƠI
        if (!AutoSellManager.isEnabled(player.getUUID())) {
            return level.addFreshEntity(entity);
        }

        ItemStack stack = itemEntity.getItem();
        long price = getFishPrice(stack);

        // 4. TỰ ĐỘNG BÁN CÁ
        if (price > 0) {
            if (!level.isClientSide()) {
                // Cộng tiền Hào vào Data
                KyoEconomyAPI.addMoneyInHao(level.getServer(), player.getUUID(), price);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                // In lên Actionbar
                serverPlayer.sendSystemMessage(Component.literal("§a+ " + price + " Hào §7(Bán " + stack.getHoverName().getString() + ")"), true);
            }
            return true; // Hủy, không rớt item ra đất
        }

        // 5. TỰ ĐỘNG LỌC RÁC
        else if (isTrash(stack)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("§cĐã tiêu hủy rác: §8" + stack.getHoverName().getString()), true);
            }
            return true; // Rác bốc hơi
        }

        // 6. VẬT PHẨM QUÝ (Sách, Yên ngựa, Cung...) -> CHO RỚT RA ĐẤT BÌNH THƯỜNG
        return level.addFreshEntity(entity);
    } // <--- CHÍNH LÀ CÁI NGOẶC NÀY ĐÃ BỊ THIẾU TRONG CODE CỦA BẠN ĐẤY!

    // --- LẤY BẢNG GIÁ VÀ RÁC TỪ FILE CONFIG ---
    // Bây giờ bạn không cần Hardcode giá trị vào Mixin nữa, tất cả đều linh hoạt lấy từ kyofishing.json
    private long getFishPrice(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return KyoFishingConfig.data.fishPrices.getOrDefault(itemId, 0L);
    }

    private boolean isTrash(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return KyoFishingConfig.data.trashItems.contains(itemId);
    }
}