# Online Shop SQL 优化与 EXPLAIN 记录

**最后更新**：2026-06-15

## 索引变更

| 表 | 索引 | 覆盖查询 |
|----|------|----------|
| `product` | `idx_product_category_id(category, id)` | 商品列表按分类过滤并按 `id` 分页 |
| `product` | `idx_product_category_price_id(category, price, id)` | 商品列表按分类、价格区间过滤和 count |
| `cart_item` | `idx_cart_item_user_id_id(user_id, id)` | 查询当前用户购物车、按明细 ID 更新/删除 |
| `orders` | `idx_orders_user_id_id(user_id, id)` | 查询用户订单列表、用户维度订单详情 |
| `orders` | `idx_orders_status_expire_at(status, expire_at)` | 超时订单扫描 |
| `order_item` | `idx_order_item_order_id_id(order_id, id)` | 查询订单明细、删除订单明细 |

## MySQL EXPLAIN 命令

```sql
EXPLAIN SELECT id, name, category, price, stock, image_url, description, created_at
FROM product
WHERE category = '数码配件'
ORDER BY id ASC
LIMIT 6 OFFSET 0;

EXPLAIN SELECT id, name, category, price, stock, image_url, description, created_at
FROM product
WHERE category = '数码配件' AND price >= 100 AND price <= 800
ORDER BY id ASC
LIMIT 6 OFFSET 0;

EXPLAIN SELECT c.id, c.user_id, c.product_id, c.quantity, p.name AS product_name, p.category,
       p.price, p.stock, p.image_url, p.description
FROM cart_item c JOIN product p ON c.product_id = p.id
WHERE c.user_id = 1
ORDER BY c.id ASC;

EXPLAIN SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone,
       receiver_address, created_at, expire_at, paid_at, updated_at
FROM orders
WHERE user_id = 1
ORDER BY id DESC;

EXPLAIN SELECT id, product_id, product_name, product_image_url, price, quantity, subtotal
FROM order_item
WHERE order_id = 1
ORDER BY id ASC;

EXPLAIN SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone,
       receiver_address, created_at, expire_at, paid_at, updated_at
FROM orders
WHERE status = 'CREATED' AND expire_at <= NOW()
ORDER BY expire_at ASC
LIMIT 100;
```

## SQLite EXPLAIN QUERY PLAN 实测

> 以下输出来自本地 SQLite profile，执行时间：2026-06-15。

| 场景 | 输出 |
|------|------|
| 商品列表 | `SEARCH product USING INDEX idx_product_category_id (category=?)` |
| 商品价格区间 | `SEARCH product USING INDEX idx_product_category_price_id (category=? AND price>? AND price<?) \| USE TEMP B-TREE FOR ORDER BY` |
| 购物车查询 | `SEARCH c USING INDEX idx_cart_item_user_id_id (user_id=?) \| SEARCH p USING INTEGER PRIMARY KEY (rowid=?)` |
| 用户订单列表 | `SEARCH orders USING INDEX idx_orders_user_id_id (user_id=?)` |
| 订单明细 | `SEARCH order_item USING INDEX idx_order_item_order_id_id (order_id=?)` |
| 超时订单扫描 | `SEARCH orders USING INDEX idx_orders_status_expire_at (status=? AND expire_at<?)` |
