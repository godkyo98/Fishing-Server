package kyofishing.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoSellManager {
    // Lưu trữ UUID của những người đang bật Auto-sell
    private static final Set<UUID> activePlayers = new HashSet<>();

    public static boolean toggle(UUID uuid) {
        if (activePlayers.contains(uuid)) {
            activePlayers.remove(uuid);
            return false; // Đã tắt
        } else {
            activePlayers.add(uuid);
            return true; // Đã bật
        }
    }

    public static boolean isEnabled(UUID uuid) {
        return activePlayers.contains(uuid);
    }
}