# Define the README content
readme_content = """# KyoFishing - Hệ thống Câu cá chuyên nghiệp cho Minecraft (Fabric 26.2)

KyoFishing là một mod **Server-side** mạnh mẽ, được thiết kế riêng để nâng cấp trải nghiệm câu cá trên TEA Server. Với kiến trúc Modular hiện đại và sự tối ưu hóa cho phiên bản Minecraft 26.2, mod mang đến cơ chế câu cá thú vị, hệ thống nổ hũ (Jackpot) kịch tính và tích hợp sâu sắc vào nền tảng kinh tế của máy chủ.

## 🌟 Tính năng Nổi bật

* **100% Server-side:** Người chơi không cần cài đặt thêm mod client. Hoạt động mượt mà trên nền tảng Fabric.
* **Hệ thống Jackpot (Nổ hũ):** Tăng phần thưởng, kèm theo cơ chế thông báo Toàn Server (Server Broadcast) và hiệu ứng Actionbar hiện đại khi người chơi trúng lớn.
* **Cân bằng tỉ lệ:** Tối ưu hóa tỉ lệ câu được "Kho báu" dựa trên cấp độ của phù phép "May Mắn Của Biển Cả" (Luck of the Sea).
* **Tối ưu hiệu năng:** Được thiết kế sạch sẽ, không gây lag (TPS ổn định), không rác bộ nhớ (Memory Leak).
* **Tích hợp đa nền tảng:** Kết nối hoàn hảo với KyoScoreboard, KyoEconomy và Placeholder API để hiển thị thông tin thời gian thực.

## 🛠️ Thông số kỹ thuật (Kỹ thuật)

* **Nền tảng:** Minecraft 26.2 - Fabric Server.
* **Mappings:** Chuẩn Mojang Mappings (Unobfuscated).
* **Kiến trúc:** Modular Architecture - Dễ dàng mở rộng và bảo trì.
* **Data Management:** Sử dụng hệ thống Codec / RecordCodecBuilder của DataFixerUpper (DFU) để lưu trữ dữ liệu an toàn, chống corrupt file.
* **Giao tiếp:** Tích hợp Placeholder API (v3.1.0+) để đồng bộ hóa dữ liệu giữa các module.

## 🎣 Tỉ lệ Câu cá (Vanilla)

| Cấp Phù Phép | Kho báu | Rác rưởi | Cá |
| :--- | :--- | :--- | :--- |
| Không có | 5.0% | 10.0% | 85.0% |
| Cấp I | ~7.1% | ~8.1% | ~84.8% |
| Cấp II | ~9.2% | ~6.1% | ~84.7% |
| Cấp III | ~11.3% | ~4.1% | ~84.5% |

> Mỗi cấp độ "May Mắn Của Biển Cả" giúp tăng ~2.1% tỉ lệ ra đồ ngon và giảm ~2% tỉ lệ rác.

## 🚀 Cài đặt & Sử dụng

1.  Đảm bảo máy chủ của bạn chạy **Fabric Loader** cho Minecraft 26.2.
2.  Tải bản build mới nhất và đưa vào thư mục `/mods` trên Server.
3.  Restart Server để hệ thống tự động sinh file cấu hình.
4.  Điều chỉnh thông số trong file config (nếu cần) và dùng lệnh `/kyofishing reload` để áp dụng thay đổi ngay lập tức.

## 🤝 Liên hệ & Đóng góp

Dự án này là một phần trong hệ sinh thái **TEA Server**. Mọi ý tưởng đóng góp hoặc báo lỗi vui lòng liên hệ trực tiếp với đội ngũ phát triển.

---
*Developed with ❤️ for TEA Server*
"""

# Save to a file
with open("README.md", "w", encoding="utf-8") as f:
    f.write(readme_content)