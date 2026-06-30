package kyofishing.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback; // <--- Import Event vào đây
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import kyofishing.manager.AutoSellManager;
import kyofishing.config.KyoFishingConfig;

public class KyoFishingCommand {

  // Hàm này giờ không cần nhận tham số dispatcher từ ngoài nữa
  public static void register() {
    // Đưa Event đăng ký lệnh vào thẳng bên trong class này
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

      // ==========================================
      // CỤM LỆNH CHÍNH (/kyofish ...)
      // ==========================================
      dispatcher.register(Commands.literal("kyofish")
          .then(Commands.literal("autosell")
              .executes(context -> toggleAutoSell(context.getSource().getPlayerOrException()))
          )
          .then(Commands.literal("as")
              .executes(context -> toggleAutoSell(context.getSource().getPlayerOrException()))
          )
          .then(Commands.literal("reload")
              .requires(source -> {
                if (source.getEntity() == null) return true;
                if (source.getEntity() instanceof ServerPlayer player) {
                  return source.getServer().getPlayerList().isOp(player.nameAndId());
                }
                return false;
              })
              .executes(context -> {
                KyoFishingConfig.load();
                context.getSource().sendSuccess(() -> Component.literal("§a[KyoFishing] Đã tải lại Bảng giá cá & Rác thành công!"), false);
                return 1;
              })
          )
      );

      // ==========================================
      // LỆNH ĐỘC LẬP SIÊU NHANH (/autosell)
      // ==========================================
      dispatcher.register(Commands.literal("autosell")
          .executes(context -> toggleAutoSell(context.getSource().getPlayerOrException()))
      );
    });
  }

  private static int toggleAutoSell(ServerPlayer player) {
    boolean isNowEnabled = AutoSellManager.toggle(player.getUUID());

    if (isNowEnabled) {
      player.sendSystemMessage(Component.literal("§e[KyoFish] §aĐã BẬT chế độ Tự Động Bán Cá & Lọc Rác. Hãy cắm AFK thoải mái!"));
    } else {
      player.sendSystemMessage(Component.literal("§e[KyoFish] §cĐã TẮT chế độ Auto-Sell."));
    }
    return 1;
  }
}