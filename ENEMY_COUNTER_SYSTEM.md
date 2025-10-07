# ☠ HỆ THỐNG ĐẾM QUÁI TIÊU DIỆT

## 📊 TÍNH NĂNG

### **Hiển thị trên màn hình:**

```
ENEMIES
☠ 4/12
```

- **Vị trí:** Góc phải trên, ngay dưới Flawless Score
- **Màu sắc:** Xanh dương (#3498DB)
- **Format:** `☠ [Đã giết]/[Tổng số]`

---

## 🎮 CƠ CHẾ HOẠT ĐỘNG

### **1. Khởi tạo:**

```kotlin
private var enemiesKilled: Int = 0
private val totalEnemies: Int
    get() = levelConfig.totalEnemies
```

### **2. Đếm khi quái chết:**

Mỗi frame, trước khi remove quái đã chết:

```kotlin
val skeletonsRemoved = skeletons.count { it.shouldBeRemoved() }
val demonsRemoved = demons.count { it.shouldBeRemoved() }
// ... các loại quái khác

enemiesKilled += tổng số quái bị remove
```

### **3. Reset khi chơi lại:**

```kotlin
enemiesKilled = 0  // Trong init() và resetGame()
```

---

## 🎨 THIẾT KẾ UI

### **Layout góc phải trên:**

```
┌────────────────────────┐
│       FLAWLESS         │
│       ❤ 2000          │  ← Flawless Score
├────────────────────────┤
│       ENEMIES          │
│       ☠ 4/12          │  ← Enemy Counter (MỚI)
└────────────────────────┘
```

### **Thông số UI:**

| Thuộc tính | Giá trị         |
| ---------- | --------------- |
| X          | width - 30f     |
| Y          | 160f            |
| Text Size  | 40f             |
| Label Size | 22f             |
| Background | rgba(0,0,0,180) |
| Shadow     | 3px black       |

---

## 📋 VÍ DỤ

### **Trong game:**

#### **Bắt đầu:**

```
ENEMIES
☠ 0/12
```

#### **Giữa chừng:**

```
ENEMIES
☠ 7/12
```

#### **Sắp thắng:**

```
ENEMIES
☠ 11/12
```

#### **Chiến thắng:**

```
ENEMIES
☠ 12/12
```

---

## 💾 DỮ LIỆU TRACKING

### **Map 1 (Grassland):**

- Tổng quái: 6
- Wave 1: 2 Skeleton
- Wave 2: 2 Skeleton + 2 Demon

### **Map 2 (Desert):**

- Tổng quái: 9
- Wave 1-4: Skeleton, Demon, Medusa

### **Map 3 (Volcano):**

- Tổng quái: 12
- Wave 1-4: Skeleton, Demon, Medusa, Jinn, Dragon

---

## 🔧 IMPLEMENTATION

### **File thay đổi:**

- ✅ `GameView.kt`
  - Thêm biến `enemiesKilled`
  - Đếm quái khi remove
  - Hiển thị UI counter
  - Reset khi start/restart

### **Logic đếm:**

```kotlin
// Trước khi remove
val removed = enemies.count { it.shouldBeRemoved() }
enemiesKilled += removed

// Sau đó remove
enemies.removeAll { it.shouldBeRemoved() }
```

---

## ✅ LỢI ÍCH

1. **Feedback rõ ràng:** Người chơi biết mình đã giết được bao nhiêu quái
2. **Tiến độ game:** Thấy được còn bao nhiêu quái cần tiêu diệt
3. **Động lực:** Thấy con số tăng dần khi chiến đấu
4. **Điều kiện thắng:** Khi `enemiesKilled == totalEnemies` → Victory

---

**Ngày tạo:** 7 tháng 10, 2025
**Version:** 1.0
