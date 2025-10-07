# 🎯 HỆ THỐNG FLAWLESS BONUS MỚI

## 📊 CƠ CHẾ HOẠT ĐỘNG

### **Khởi đầu:**

- Mỗi game bắt đầu với **2000 điểm Flawless**

### **Khi bị đánh:**

- Mỗi lần nhân vật bị quái đánh trúng: **-100 điểm**
- Điểm tối thiểu: **0** (không âm)

### **Kết thúc game:**

- Điểm Flawless còn lại được **cộng vào tổng điểm**
- Tính vào `bonusScore` và `totalScore`

---

## 🎨 HIỂN THỊ TRONG GAME

### **Màn hình chơi (góc phải trên):**

```
FLAWLESS
❤ 2000  (Xanh lá - Hoàn hảo)
```

### **Mã màu theo điểm:**

| Điểm còn lại | Màu sắc     | Ý nghĩa    |
| ------------ | ----------- | ---------- |
| ≥ 1500       | 🟢 Xanh lá  | Xuất sắc   |
| 1000-1499    | 🟡 Vàng cam | Tốt        |
| 500-999      | 🟠 Cam      | Trung bình |
| 1-499        | 🔴 Đỏ       | Nguy hiểm  |
| 0            | ⚫ Xám      | Hết điểm   |

---

## 🏆 MÀN HÌNH CHIẾN THẮNG

### **Hiển thị bonus:**

```
✓ Flawless Bonus: +1800  (nếu còn 1800/2000)
✓ Flawless Bonus: +500   (nếu còn 500/2000)
✓ Flawless Bonus: +0     (nếu hết điểm)
```

### **Ví dụ tính điểm:**

#### **Trường hợp 1: Không bị đánh**

- Base Score: 600 (6 quái × 100)
- Flawless Bonus: +2000
- Time Bonus: +1000
- **Total: 3600 điểm**

#### **Trường hợp 2: Bị đánh 5 lần**

- Base Score: 600
- Flawless Bonus: +1500 (2000 - 5×100)
- Time Bonus: +1000
- **Total: 3100 điểm**

#### **Trường hợp 3: Bị đánh 25 lần**

- Base Score: 600
- Flawless Bonus: +0 (đã hết)
- Time Bonus: +1000
- **Total: 1600 điểm**

---

## 💾 DỮ LIỆU LƯU TRỮ

### **VictoryRecord:**

```kotlin
data class VictoryRecord(
    val flawlessScore: Int = 0,    // Điểm Flawless còn lại
    val achievedNoHitBonus: Boolean // true nếu flawlessScore > 0
)
```

---

## 📝 CÁC FILE THAY ĐỔI

### 1. **GameView.kt**

- Đổi `playerWasHit: Boolean` → `flawlessScore: Int`
- Thêm hàm `onPlayerHit()` trừ 100 điểm
- Hiển thị Flawless Score trên UI (góc phải trên)
- Reset `flawlessScore = 2000` khi start/restart game

### 2. **GameModeConfig.kt**

- Đổi parameter `wasHit: Boolean` → `flawlessScore: Int`
- Logic bonus: `totalScore += flawlessScore`
- Check: `noHitBonus = (flawlessScore > 0)`

### 3. **VictoryRecord.kt**

- Thêm field: `flawlessScore: Int`

### 4. **LevelVictoryDialog.kt**

- Hiển thị động: `"✓ Flawless Bonus: +$flawlessPoints"`

---

## 🎮 GAMEPLAY IMPACT

### **Khuyến khích:**

- ✅ Tránh né quái
- ✅ Timing tấn công chính xác
- ✅ Sử dụng skill phòng thủ (Shield/Bow)

### **Hình phạt:**

- ❌ Mỗi lần bị đánh = mất 100 điểm
- ❌ 20 lần bị đánh = mất toàn bộ bonus

### **Điểm cân bằng:**

- Dễ chơi: Vẫn có thể thắng khi bị đánh
- Khó master: Cần kỹ năng cao để giữ 2000 điểm

---

## ✅ KẾT QUẢ

| Kịch bản               | Flawless Score | Cảm giác         |
| ---------------------- | -------------- | ---------------- |
| Perfect run (0 hit)    | 2000           | 🏆 Bậc thầy      |
| Good run (1-5 hits)    | 1500-1900      | 😊 Tốt           |
| Normal run (6-15 hits) | 500-1400       | 😐 Trung bình    |
| Bad run (16-20 hits)   | 0-400          | 😰 Cần cải thiện |
| Very bad (>20 hits)    | 0              | 😢 Không bonus   |

---

**Ngày cập nhật:** 7 tháng 10, 2025
**Version:** 2.0
