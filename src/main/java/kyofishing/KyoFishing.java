package kyofishing;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import kyofishing.command.KyoFishingCommand;
import kyofishing.config.KyoFishingConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kyofishing.data.KyoFishingState;

public class KyoFishing implements ModInitializer {
	public static final String MOD_ID = "kyofishing";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[KyoFishing] Đang khởi động hệ thống Cày Cuốc...");
		// 1. Nạp Config
		KyoFishingConfig.load();

		// 2. Khởi tạo Lệnh (Chỉ đúng 1 dòng siêu gọn gàng!)
		KyoFishingCommand.register();

		// 3. Placeholder cho Cấp độ: %kyofishing:level% (CÓ TỰ ĐỘNG ĐỔI MÀU)
		Placeholders.registerServer(Identifier.fromNamespaceAndPath(MOD_ID, "level"), (context, arg) -> {
			if (!context.hasPlayer()) return PlaceholderResult.invalid("No player");
			ServerPlayer player = (ServerPlayer) context.player();
			KyoFishingState state = KyoFishingState.getServerState(player.level().getServer());

			int level = state.getFishingLevel(player.getUUID());
			String colorCode = "§f"; // Mặc định Trắng (Tân thủ)

			// Đặt các mốc màu sắc (Tùy bạn sáng tạo nhé!)
			if (level >= 3000) {
				colorCode = "§4§l"; // Đỏ sậm in đậm (Chúa tể)
			} else if (level >= 2000) {
				colorCode = "§c";   // Đỏ (Bậc thầy)
			} else if (level >= 1000) {
				colorCode = "§6";   // Cam (Chuyên gia)
			} else if (level >= 500) {
				colorCode = "§e";   // Vàng (Tinh anh)
			} else if (level >= 200) {
				colorCode = "§b";   // Xanh lơ (Thợ săn)
			} else if (level >= 100) {
				colorCode = "§a";   // Xanh lá (Tập sự)
			}

			// Trả về số cấp độ đã kèm màu
			return PlaceholderResult.value(colorCode + level);
		});

		// 4. Placeholder cho Số cá câu được: %kyofishing:caught% (CÓ TỰ ĐỘNG ĐỔI MÀU)
		Placeholders.registerServer(Identifier.fromNamespaceAndPath(MOD_ID, "caught"), (context, arg) -> {
			if (!context.hasPlayer()) return PlaceholderResult.invalid("No player");
			ServerPlayer player = (ServerPlayer) context.player();
			KyoFishingState state = KyoFishingState.getServerState(player.level().getServer());

			int caught = state.getFishCount(player.getUUID());
			String colorCode = "§f"; // Mặc định Trắng

			// Tương tự, đặt mốc màu cho số lượng cá
			if (caught >= 1000000) {
				colorCode = "§5"; // Tím (Thủy quái)
			} else if (caught >= 500000) {
				colorCode = "§d"; // Hồng (Ngư phủ)
			} else if (caught >= 100000) {
				colorCode = "§3"; // Lục (Thợ lặn)
			} else if (caught >= 10000) {
				colorCode = "§9"; // Lam (Người chài lưới)
			}

			// Trả về số cá đã kèm màu
			return PlaceholderResult.value(colorCode + caught);
		});
	}
}