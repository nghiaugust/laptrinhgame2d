# LASER BEAM SKILL SYSTEM

## Tổng quan

Hệ thống skill Laser Beam (Cột Laze) - kỹ năng bắn tia laser vào quái gần nhất, gây 200 damage.

## Các file đã tạo

### 1. LaserBeamSkillItem.kt

**Vị trí:** `laptrinhgame2d/app/src/main/java/com/example/laptrinhgame2d/items/skills/LaserBeamSkillItem.kt`

**Chức năng:**

- Vật phẩm skill rơi ra khi đánh quái (tỉ lệ 50%)
- Không tự động nhặt, cần đến gần và bấm nút "NHẶT"
- Hiệu ứng visual: Core trắng-đỏ với gradient, 16 energy particles quay tròn, 4 rotating beams
- Animation: Float lên xuống, xoay, pulse, glow
- Tồn tại 15 giây nếu không nhặt

**Đặc điểm:**

- Màu sắc: Trắng -> Đỏ gradient
- Kích thước: baseSize = 50px
- Particles: 16 energy particles với góc, khoảng cách, tốc độ khác nhau
- Beams: 4 rotating beams xoay 360 độ

### 2. LaserBeamEffect.kt

**Vị trí:** `laptrinhgame2d/app/src/main/java/com/example/laptrinhgame2d/items/skills/LaserBeamEffect.kt`

**Chức năng:**

- Hiệu ứng laser beam bắn từ hero đến target
- Animation 3 giai đoạn (90 frames = 1.5 giây):
  - Phase 1 (0-30 frames): Laser nhỏ xuất hiện và to dần (5px -> 60px)
  - Phase 2 (30-60 frames): Laser full size, deal damage 200 HP
  - Phase 3 (60-90 frames): Laser mờ dần và biến mất
- Impact effect tại vị trí quái: vòng tròn nổ + 20 particles bay ra

**Thông số:**

- Total damage: 200 HP (dealt một lần duy nhất ở phase 2)
- Beam width: 5px -> 60px -> fade out
- Impact radius: 0 -> 80px
- Duration: 1.5 giây (90 frames @ 60 FPS)
- Màu sắc: 3 layers (outer glow đỏ, middle trắng-đỏ, core trắng)

### 3. LaserBeamSkillButton.kt

**Vị trí:** `laptrinhgame2d/app/src/main/java/com/example/laptrinhgame2d/items/skills/LaserBeamSkillButton.kt`

**Chức năng:**

- Nút UI để kích hoạt skill sau khi nhặt
- Cooldown: 30 giây (1800 frames)
- Hiển thị thời gian cooldown còn lại (số giây)
- Animation pulse khi ready

**Vị trí:**

- X: screenWidth - 500px (bên trái nút Black Hole)
- Y: screenHeight - 200px
- Size: 80px radius

**Visual:**

- Background: Radial gradient đỏ khi ready, xám khi cooldown
- Icon: Core trắng + 4 laser beams xoay
- Cooldown overlay: Semi-transparent + countdown text

### 4. Cập nhật ItemDropConfig.kt

**Thêm:**

```kotlin
enum class ItemType {
    ...
    LASER_BEAM_SKILL,   // Skill laser beam
}

fun shouldDropLaserBeamSkill(): Boolean {
    return Math.random() < 0.5  // 50%
}
```

### 5. Tích hợp vào GameView.kt

**Các biến mới:**

```kotlin
private val laserBeamSkillItems = mutableListOf<LaserBeamSkillItem>()
private val laserBeamEffects = mutableListOf<LaserBeamEffect>()
private var hasLaserBeamSkill = false
private var laserBeamSkillButton: LaserBeamSkillButton? = null
```

**Các hàm mới:**

#### `unlockLaserBeamSkill()`

- Tạo button skill tại vị trí (screenWidth - 500, screenHeight - 200)
- Set flag hasLaserBeamSkill = true

#### `castLaserBeam()`

- Tìm quái gần nhất bằng `findNearestEnemy()`
- Tạo LaserBeamEffect từ player đến target
- Start cooldown button

#### `findNearestEnemy(playerX, playerY): Any?`

- Duyệt qua tất cả loại quái: skeletons, demons, medusas, jinns, smallDragons, dragons
- Tính khoảng cách Euclidean đến từng quái
- Trả về enemy gần nhất (hoặc null nếu không có)

#### `applyLaserBeamDamage(effect)`

- Lấy vị trí target từ effect
- Tìm quái tại vị trí đó (tolerance ±50px)
- Gọi damage function tương ứng: `damageSkeletonAndCheck()`, `damageDemonAndCheck()`, etc.
- Deal damage 200 HP một lần duy nhất

**Cập nhật trySpawnItem():**

```kotlin
if (ItemDropConfig.shouldDropLaserBeamSkill()) {
    laserBeamSkillItems.add(LaserBeamSkillItem(gameContext, x, groundY - 150f))
}
```

**Cập nhật updateItems():**

- Update laser skill items, remove expired
- Update laser effects, apply damage
- Update laser button

**Cập nhật checkSkillPickupRange():**

- Kiểm tra cả blackHoleSkillItems và laserBeamSkillItems
- Hiển thị nút "NHẶT" cho skill gần nhất

**Cập nhật tryPickupSkill():**

- Kiểm tra cả 2 loại skill
- Gọi `unlockLaserBeamSkill()` khi nhặt laser skill

**Cập nhật draw():**

- Vẽ laserBeamSkillItems (world space)
- Vẽ laserBeamEffects (world space)
- Vẽ laserBeamSkillButton (UI space)

**Cập nhật onTouchEvent():**

- Xử lý touch event cho laserBeamSkillButton
- Kiểm tra isReady() trước khi cast
- Gọi castLaserBeam()

## Cách hoạt động

### 1. Drop Item

- Khi giết quái, có 50% rơi ra Laser Beam skill item
- Item xuất hiện tại vị trí quái chết, groundY - 150px
- Item có hiệu ứng visual đẹp mắt (trắng-đỏ, particles, beams)

### 2. Pickup

- Player đến gần item trong phạm vi 150px
- Nút "NHẶT" hiện ra phía trên item
- Bấm nút để nhặt

### 3. Unlock

- Sau khi nhặt, tự động tạo skill button ở góc dưới màn hình
- Button màu đỏ với icon laser beams xoay
- Sẵn sàng sử dụng ngay

### 4. Cast Skill

- Bấm vào skill button
- Hệ thống tự tìm quái gần nhất
- Nếu có quái:
  - Tạo laser beam từ player đến quái
  - Laser nhỏ xuất hiện, to dần trong 0.5s
  - Laser giữ full size 0.5s, deal 200 damage
  - Laser mờ dần và biến mất trong 0.5s
  - Impact effect tại vị trí quái (explosion + particles)
- Start cooldown 30 giây

### 5. Cooldown

- Button chuyển sang màu xám
- Hiển thị countdown (30, 29, 28, ...)
- Không thể bấm cho đến khi hết cooldown

## So sánh với Black Hole Skill

| Tính năng       | Black Hole                    | Laser Beam                    |
| --------------- | ----------------------------- | ----------------------------- |
| Drop rate       | 50%                           | 50%                           |
| Pickup          | Manual (nút NHẶT)             | Manual (nút NHẶT)             |
| Target          | AOE (600px radius)            | Single target (quái gần nhất) |
| Damage          | 100 HP total (4 HP x 25 hits) | 200 HP instant                |
| Duration        | 5 giây                        | 1.5 giây                      |
| Cooldown        | 10 giây                       | 30 giây                       |
| Spawn           | 300px từ player               | Tại vị trí player             |
| Effect          | Hút + damage liên tục         | Laser beam instant            |
| Button position | screenWidth - 350             | screenWidth - 500             |
| Màu sắc         | Tím                           | Trắng-Đỏ                      |

## Lưu ý kỹ thuật

### Animation Timing

- 60 FPS target
- Phase 1: 30 frames = 0.5s (appear + grow)
- Phase 2: 30 frames = 0.5s (full damage)
- Phase 3: 30 frames = 0.5s (fade out)

### Damage Application

- Damage chỉ deal một lần ở phase 2
- Flag `damageDealt` đảm bảo không deal damage nhiều lần
- Tolerance ±50px để hit detection chính xác

### Performance

- Laser effect tự xóa sau khi hết active
- Impact particles tự fade (alpha \*= 0.92)
- Không có memory leak

### Visual Layers

Laser beam có 3 layers:

1. Outer glow (red, wider)
2. Middle layer (white-red gradient)
3. Core (pure white, thinner)

Impact có 2 layers:

1. Expanding ring (red gradient)
2. Core flash (white)

## Testing Checklist

✅ Item spawn đúng 50% rate
✅ Pickup button hiện khi ở gần
✅ Nhặt được skill, tạo button
✅ Button hiển thị đúng vị trí
✅ Tìm được quái gần nhất
✅ Laser animation 3 phases smooth
✅ Damage 200 HP dealt đúng lúc
✅ Impact effect đẹp
✅ Cooldown 30s hoạt động
✅ Multi-skill support (có thể dùng cùng Black Hole)
✅ No compilation errors
