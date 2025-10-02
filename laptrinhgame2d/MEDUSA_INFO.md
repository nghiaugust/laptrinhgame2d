# Medusa Enemy - Thông tin chi tiết

## Tổng quan

Medusa là một ranged enemy (kẻ địch đánh xa) với chiến thuật kiting - chạy trốn khi hero đến gần và ném đá tấn công từ xa.

## Thống kê

- **HP**: 60 (thấp nhất trong các enemy)
- **Speed**: 5f (nhanh nhất - để chạy trốn)
- **Attack Damage**: 15 (tấn công thường)
- **Skill Damage**: 25 (skill mạnh hơn)
- **Attack Range**: 300f (tầm đánh xa)
- **Skill Range**: 400f (tầm skill xa hơn)
- **Retreat Distance**: 200f (khoảng cách bắt đầu chạy trốn)
- **Safe Distance**: 350f (khoảng cách an toàn muốn duy trì)
- **Detection Range**: 600f (phát hiện rất xa)

## Hoạt ảnh (Animations)

1. **Idle**: 3 frames - Đứng yên
2. **Walk**: 4 frames - Di chuyển (chạy trốn, tiến lại, patrol)
3. **Attack**: 6 frames - Tấn công thường (ném đá, spawn stone ở frame 3)
4. **Skill**: 6 frames - Sử dụng skill (dùng chung attack animation, spawn stone ở frame 4)
5. **Hurt**: 2 frames - Bị thương
6. **Death**: 6 frames (trong folder "deadth")

**Lưu ý**: Skill không có animation riêng, sử dụng chung attack animation. Stone1-8 trong folder skill là animation của viên đá bay (projectile), không phải animation của Medusa.

## Hệ thống đá (Stone Projectiles)

### Stone Animation

- **Flying**: Stone1-2 (2 frames) - Bay trên không
- **Breaking**: Stone3-8 (6 frames) - Vỡ khi trúng mục tiêu

### Cơ chế hoạt động

1. Medusa tấn công → spawn Stone ở frame 3 (attack) hoặc frame 4 (skill)
2. Stone tính toán hướng bay về phía player (velocityX, velocityY dựa trên vị trí player)
3. Stone bay với tốc độ 15f/frame theo hướng đã tính
4. Khi Stone va chạm với hero → gây damage và chuyển sang animation vỡ (Stone3-8)
5. Sau khi animation vỡ kết thúc → Stone bị xóa

## AI Behavior (Kiting Style)

### Các trạng thái AI:

1. **Skill Mode** (distance ≤ 400f && skillCooldown = 0):

   - Dùng skill ném đá mạnh
   - Damage: 25
   - Cooldown: 120 frames (2 giây)

2. **Attack Mode** (distance ≤ 300f && attackCooldown = 0 && skillCooldown > 60):

   - Tấn công thường ném đá
   - Damage: 15
   - Cooldown: 50 frames (0.83 giây)

3. **Retreat Mode** (distance < 200f):

   - Chạy trốn ra xa hero
   - Sử dụng WALK animation
   - Quay lưng với hero khi chạy

4. **Hold Position** (200f ≤ distance < 350f):

   - Giữ khoảng cách an toàn
   - IDLE animation
   - Quay mặt về phía hero

5. **Approach Mode** (350f ≤ distance < 600f):

   - Di chuyển lại gần để tấn công
   - WALK animation
   - Tiến về phía hero

6. **Patrol Mode** (distance ≥ 600f):
   - Di chuyển qua lại ±30 pixels
   - WALK animation
   - Đổi hướng mỗi 120 frames (2 giây)

## Vị trí spawn

- 2200f, 400f
- 3500f, 600f
- 4000f, 500f

(Spawn ở khu vực xa hơn các enemy khác vì là ranged enemy)

## Tương tác với hero

### Medusa → Hero:

- Stones gây 15-25 damage tùy loại tấn công
- Stones vỡ khi trúng hero (animation Stone3-8)

### Hero → Medusa:

- Melee attack: Kiểm tra range 200f, phải quay mặt về Medusa
- Arrows (Samurai_Archer): 15 damage
- Skill projectiles (Samurai_Archer): Damage theo skill

## Đặc điểm chiến đấu

1. **Kiting**: Chạy trốn khi hero đến gần (<200f)
2. **Ranged attacker**: Tấn công từ xa bằng đá
3. **Low HP**: Chỉ 60 HP, dễ bị tiêu diệt nếu hero tiếp cận được
4. **Fast movement**: Speed 5f để trốn thoát
5. **Dual attack**: Có cả tấn công thường và skill

## Code files

- `Medusa.kt`: Main enemy class với AI kiting
- `Stone.kt`: Projectile class cho đá ném
- `GameView.kt`: Integration vào game loop
  - Update: Line ~380-400
  - Collision: Line ~425-445, ~475-520
  - Draw: Line ~555-560
