package com.jdcr.jdcrcompose.data

data class Product(
    val id: Long,
    val name: String,
    val category: String,
    val summary: String,
    val description: String,
    val priceInCents: Int,
    val rating: Double,
)

object ProductCatalog {
    val products = listOf(
        Product(
            id = 1,
            name = "AirNote 降噪耳机",
            category = "音频",
            summary = "轻量佩戴，支持自适应降噪",
            description = "为通勤和长时间工作设计的无线耳机，提供清晰的人声表现、稳定连接和舒适佩戴体验。",
            priceInCents = 89900,
            rating = 4.8,
        ),
        Product(
            id = 2,
            name = "Arc Mini 机械键盘",
            category = "效率",
            summary = "75% 配列，三模连接",
            description = "紧凑布局保留常用功能键，支持有线、蓝牙和 2.4G 连接，适合桌面办公与移动使用。",
            priceInCents = 59900,
            rating = 4.7,
        ),
        Product(
            id = 3,
            name = "Lumen 随行灯",
            category = "家居",
            summary = "无级调光，最长续航 24 小时",
            description = "柔和漫射光适合阅读、床头和露营场景。旋钮调光直观，内置电池支持 USB-C 充电。",
            priceInCents = 32900,
            rating = 4.9,
        ),
        Product(
            id = 4,
            name = "Fold 旅行收纳包",
            category = "出行",
            summary = "分区收纳，防泼水面料",
            description = "内部采用可视网袋和独立线材分区，能够整齐收纳充电器、移动电源及日常小物。",
            priceInCents = 15900,
            rating = 4.6,
        ),
    )

    fun find(productId: Long): Product? = products.firstOrNull { it.id == productId }
}
