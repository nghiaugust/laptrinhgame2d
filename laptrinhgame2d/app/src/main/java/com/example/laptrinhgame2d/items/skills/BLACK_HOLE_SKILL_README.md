# Black Hole Skill System - Hệ thống Kỹ năng Hố Đen

## 📁 Cấu trúc File

```
items/
  skills/
    ├── BlackHoleSkillItem.kt      # Vật phẩm skill rơi ra từ quái
    ├── BlackHoleEffect.kt         # Hiệu ứng hố đen triệu hồi
    ├── BlackHoleSkillButton.kt    # Nút UI để kích hoạt skill
    └── PickupButton.kt            # Nút nhặt vật phẩm
```

## 🎮 Cách Hoạt Động

### 1. **Rơi Vật Phẩm Skill**

- Khi đánh chết quái có **50% tỉ lệ** rơi vật phẩm skill Black Hole
- Vật phẩm xuất hiện tại vị trí quái chết
- Tồn tại **15 giây** rồi biến mất

### 2. **Nhặt Skill**

- **KHÔNG tự động nhặt** như health heart
- Phải đến gần (trong phạm vi 150 pixels)
- Xuất hiện **nút "NHẶT"** màu xanh lá
- Bấm nút để nhặt skill

### 3. **Unlock Skill**

- Khi nhặt thành công:
  - Nút skill Black Hole xuất hiện gần các nút tấn công
  - Icon hố đen tím với animation xoay

### 4. **Sử Dụng Skill**

- Bấm nút skill để kích hoạt
- Triệu hồi hố đen **cách hero 300 pixels** về phía đang nhìn
- Cooldown: **10 giây**

### 5. **Hiệu Ứng Hố Đen**

- **Phạm vi hút**: 600 pixels
- **Thời gian duy trì**: 5 giây
- **Damage**: 20 HP/giây cho tất cả quái trong phạm vi
- **Lực hút**: Kéo quái về phía tâm hố đen

## ⚙️ Thông Số Kỹ Thuật

### BlackHoleSkillItem

- Kích thước: 50px
- Lifetime: 900 frames (15 giây @ 60 FPS)
- Animation: Xoay + float + particles

### BlackHoleEffect

- Core size: 80px
- Pull range: 600px
- Duration: 300 frames (5 giây @ 60 FPS)
- Damage: 20 HP/second
- Pull strength: 8 pixels/frame (mạnh hơn khi gần tâm)

### BlackHoleSkillButton

- Radius: 80px
- Cooldown: 600 frames (10 giây @ 60 FPS)
- Màu: Tím gradient
- Icon: Mini black hole

### PickupButton

- Radius: 70px
- Text: "NHẶT"
- Màu: Xanh lá (#32C832)
- Icon: Bàn tay

## 🎨 Hiệu Ứng Visual

### Skill Item (Vật Phẩm Rơi)

- Hố đen tím với gradient
- 12 particles xoay quanh
- Spiral animation
- Pulse effect
- 3 vòng sáng xoay

### Black Hole Effect (Hiệu Ứng Triệu Hồi)

- Tâm đen tuyền với gradient tím
- 30 spiral particles hút vào tâm
- 4 vòng tròn xoay
- Vòng tròn phạm vi mờ
- Pulse animation

## 💻 Tích Hợp vào GameView

### 1. Import

```kotlin
import com.example.laptrinhgame2d.items.skills.*
```

### 2. Thêm Variables

```kotlin
// Skill items
private val blackHoleSkillItems = mutableListOf<BlackHoleSkillItem>()

// Black hole effects
private val blackHoleEffects = mutableListOf<BlackHoleEffect>()

// UI Buttons
private val pickupButton: PickupButton
private var blackHoleSkillButton: BlackHoleSkillButton? = null
private var hasBlackHoleSkill = false
```

### 3. Spawn Skill Item Khi Quái Chết

```kotlin
private fun trySpawnItem(x: Float, y: Float) {
    // Health heart (50%)
    if (ItemDropConfig.shouldDropItem(0.5f)) {
        // ... spawn health heart
    }

    // Black hole skill (50%)
    if (ItemDropConfig.shouldDropBlackHoleSkill()) {
        val groundY = when (mapType) {
            2 -> desertMap?.groundY ?: (height * 0.75f)
            3 -> volcanoMap?.groundY ?: (height * 0.75f)
            else -> grasslandMap?.groundY ?: (height * 0.75f)
        }
        blackHoleSkillItems.add(BlackHoleSkillItem(gameContext, x, groundY - 150f))
    }
}
```

### 4. Update Logic

```kotlin
fun update() {
    // Update skill items
    blackHoleSkillItems.forEach { it.update() }
    blackHoleSkillItems.removeAll { it.shouldBeRemoved() }

    // Update black hole effects
    blackHoleEffects.forEach { effect ->
        effect.update()

        // Hút và damage quái
        if (effect.isActive()) {
            applyBlackHoleEffects(effect)
        }
    }
    blackHoleEffects.removeAll { !it.isActive() }

    // Update buttons
    pickupButton.update()
    blackHoleSkillButton?.update()

    // Check pickup range
    checkSkillPickupRange(playerX, playerY)
}
```

### 5. Apply Black Hole Effects

```kotlin
private fun applyBlackHoleEffects(effect: BlackHoleEffect) {
    // Áp dụng lực hút và damage cho tất cả quái
    allEnemies.forEach { enemy ->
        if (!enemy.isDead() && effect.isInRange(enemy.getX(), enemy.y)) {
            // Hút quái về phía hố đen
            val (pullX, pullY) = effect.getPullForce(enemy.getX(), enemy.y)
            enemy.applyForce(pullX, pullY)

            // Gây damage mỗi giây
            if (effect.shouldDealDamage()) {
                enemy.takeDamage(effect.getDamage())
            }
        }
    }
}
```

### 6. Touch Event

```kotlin
override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            // Pickup button
            if (pickupButton.isVisible() && pickupButton.isPressed(x, y)) {
                pickupButton.onTouch(pointerId)
                tryPickupSkill()
            }

            // Black hole skill button
            if (hasBlackHoleSkill && blackHoleSkillButton?.isPressed(x, y) == true) {
                if (!blackHoleSkillButton!!.isOnCooldown()) {
                    blackHoleSkillButton!!.onTouch(pointerId)
                    castBlackHole()
                }
            }
        }

        MotionEvent.ACTION_UP -> {
            pickupButton.reset()
            blackHoleSkillButton?.reset()
        }
    }
}
```

### 7. Cast Black Hole

```kotlin
private fun castBlackHole() {
    val spawnDistance = 300f
    val direction = if (playerFacingRight) 1 else -1
    val spawnX = playerX + spawnDistance * direction
    val spawnY = playerY

    blackHoleEffects.add(BlackHoleEffect(spawnX, spawnY))
    blackHoleSkillButton?.startCooldown()
}
```

### 8. Draw

```kotlin
override fun draw(canvas: Canvas) {
    // ... vẽ map

    canvas.save()
    canvas.translate(-cameraX, -cameraY)

    // Vẽ skill items
    blackHoleSkillItems.forEach { it.draw(canvas) }

    // Vẽ black hole effects
    blackHoleEffects.forEach { it.draw(canvas) }

    // ... vẽ enemies, heroes

    canvas.restore()

    // Vẽ UI buttons (không bị ảnh hưởng bởi camera)
    pickupButton.draw(canvas)
    blackHoleSkillButton?.draw(canvas)
}
```

## 🎯 Checklist Tích Hợp

- [ ] Import các class skill
- [ ] Thêm variables vào GameView
- [ ] Cập nhật trySpawnItem() để spawn skill
- [ ] Thêm update logic cho skill items và effects
- [ ] Implement applyBlackHoleEffects()
- [ ] Xử lý touch event cho pickup và skill button
- [ ] Implement castBlackHole()
- [ ] Vẽ skill items, effects, và buttons
- [ ] Reset skill khi reset game

## 🔧 Tùy Chỉnh

Để thay đổi thông số, sửa trong các file:

**ItemDropConfig.kt** - Tỉ lệ rơi:

```kotlin
fun shouldDropBlackHoleSkill(): Boolean {
    return Math.random() < 0.5  // 50% → thay đổi ở đây
}
```

**BlackHoleEffect.kt** - Damage và thời gian:

```kotlin
private val damagePerSecond = 20     // Damage/giây
private val maxLifetime = 300        // 5 giây
private val pullRange = 600f         // Phạm vi hút
```

**BlackHoleSkillButton.kt** - Cooldown:

```kotlin
private val cooldownDuration = 600   // 10 giây
```

## ✅ Hoàn thành!

Hệ thống Black Hole Skill đã sẵn sàng để tích hợp vào game!
