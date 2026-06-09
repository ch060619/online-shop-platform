INSERT OR REPLACE INTO user (id, username, password, nickname, phone)
VALUES (1, 'demo', 'demo123', '演示用户', '13800000000');

INSERT OR REPLACE INTO product (id, name, category, price, stock, image_url, description)
VALUES
(1, '机械键盘 K87', '数码配件', 299.00, 35, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80', '87 键热插拔机械键盘，适合办公和游戏。'),
(2, '轻量跑鞋 AirRun', '运动户外', 459.00, 24, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', '透气网面与缓震中底，适合日常训练。'),
(3, '陶瓷马克杯', '生活家居', 39.90, 100, 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=800&q=80', '350ml 简约陶瓷杯，微波炉可用。'),
(4, '蓝牙降噪耳机', '数码配件', 699.00, 18, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', '主动降噪，续航 30 小时。'),
(5, '棉质连帽卫衣', '服饰鞋包', 169.00, 42, 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=800&q=80', '宽松版型，亲肤棉质面料。'),
(6, '桌面护眼台灯', '生活家居', 129.00, 50, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80', '三档色温，无频闪照明。');
