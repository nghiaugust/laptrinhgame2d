# Game Map System - Hệ thống Map góc nhìn ngang

## Tổng quan

Game đã được cập nhật với hệ thống map góc nhìn ngang (side-scrolling) bao gồm:

- **Background layers với parallax scrolling** (hiệu ứng chiều sâu)
- **Ground platform** (dải đất phía dưới)
- **Camera theo nhân vật** chỉ theo trục X (ngang)

## Cấu trúc các class

### 1. GameMap.kt

Class chính quản lý toàn bộ map system

- Quản lý 4 layers: Sky, Mountains, Hills, Ground
- Mỗi layer có tốc độ parallax khác nhau
- `groundY`: Vị trí mặt đất (70% chiều cao màn hình)

### 2. BackgroundLayer.kt

Xử lý các layer background với parallax scrolling

- `scrollSpeed`: Tốc độ cuộn (0.1 - 1.0)
  - Sky: 0.1 (chậm nhất - xa nhất)
  - Mountains: 0.3 (chậm)
  - Hills: 0.6 (nhanh hơn - gần hơn)
- Tự động lặp lại ảnh để tạo map vô hạn

### 3. Ground.kt

Xử lý dải đất phía dưới

- Cuộn theo camera với tỷ lệ 1:1
- Tile hóa để tạo độ dài vô hạn
- Nhân vật và quái đứng trên mặt này

### 4. BackgroundGenerator.kt

Tự động tạo các ảnh background nếu chưa có trong assets

- `generateSkyBackground()`: Bầu trời xanh với mây trắng
- `generateMountainsBackground()`: Núi xa màu xanh nhạt
- `generateHillsBackground()`: Đồi gần màu xanh lá
- `generateGroundTexture()`: Đất nâu với cỏ xanh

## Parallax Scrolling Effect

Hiệu ứng chiều sâu tạo cảm giác 3D:

```
Camera di chuyển 100 pixels →
- Sky di chuyển 10 pixels (scrollSpeed = 0.1)
- Mountains di chuyển 30 pixels (scrollSpeed = 0.3)
- Hills di chuyển 60 pixels (scrollSpeed = 0.6)
- Ground di chuyển 100 pixels (scrollSpeed = 1.0)
```

## Cách hoạt động

### Trong GameView.kt:

1. **Khởi tạo map** trong `surfaceCreated()`:

   ```kotlin
   gameMap = GameMap(gameContext, width, height)
   ```

2. **Update map** trong `update()`:

   ```kotlin
   gameMap?.update(cameraX, cameraY)
   ```

3. **Giữ nhân vật trên mặt đất**:

   ```kotlin
   val groundY = gameMap?.groundY ?: (height * 0.7f)
   fighter?.y = groundY - 200f
   ```

4. **Camera theo nhân vật** (chỉ trục X):

   ```kotlin
   cameraX = playerX - width / 2f
   cameraY = groundY - height * 0.6f // Cố định Y
   ```

5. **Vẽ map** trong `draw()`:
   ```kotlin
   gameMap?.draw(canvas, cameraX, cameraY)
   ```

## Thay đổi trong gameplay

### Trước (Top-down view):

- Nhân vật di chuyển tự do 8 hướng (X và Y)
- Camera theo nhân vật cả X và Y
- Không có ground

### Sau (Side-scrolling view):

- Nhân vật chỉ di chuyển trái/phải (X)
- Nhân vật luôn đứng trên mặt đất (Y cố định)
- Camera chỉ theo trục X, Y cố định
- Background có hiệu ứng parallax
- Có ground platform rõ ràng

## Assets

Background images tự động được tạo bởi `BackgroundGenerator`:

- Lưu tại: `context.filesDir/backgrounds/`
- Files: `sky.png`, `mountains.png`, `hills.png`, `ground.png`

### Nếu muốn dùng ảnh tùy chỉnh:

Đặt ảnh vào thư mục: `app/src/main/assets/backgrounds/`

#### Danh sách ảnh cần thiết:

| Tên File        | Mục đích                  | Kích thước đề xuất           | Yêu cầu                                                                              |
| --------------- | ------------------------- | ---------------------------- | ------------------------------------------------------------------------------------ |
| `sky.png`       | Bầu trời (layer xa nhất)  | **1920 x 1080** hoặc lớn hơn | Nền xanh nhạt, có thể có mây, mặt trời. Nên dùng ảnh rộng để tránh lặp lại quá nhiều |
| `mountains.png` | Núi xa (layer giữa 1)     | **1920 x 1080** hoặc lớn hơn | Núi màu xanh/xám nhạt, có thể trong suốt (PNG alpha). Silhouette đơn giản            |
| `hills.png`     | Đồi gần (layer giữa 2)    | **1920 x 1080** hoặc lớn hơn | Đồi màu xanh lá đậm hơn núi. Có thể có cây cối. Nên trong suốt (PNG alpha)           |
| `ground.png`    | Dải đất (ground platform) | **512 x 512** (tileable)     | Đất/cỏ, phải **tile được** (seamless). Sẽ lặp lại theo chiều ngang                   |

#### Chi tiết từng loại ảnh:

**1. sky.png** (Bầu trời)

- **Kích thước**: 1920x1080 hoặc 2560x1440 (càng rộng càng tốt)
- **Định dạng**: PNG hoặc JPG
- **Màu sắc**: Xanh nhạt (sky blue), gradient từ trên xuống
- **Nội dung**: Bầu trời trong, có thể có:
  - Mây trắng nhỏ
  - Mặt trời/mặt trăng
  - Độ gradient đẹp
- **Lưu ý**: Không cần trong suốt, đây là layer nền nhất

**2. mountains.png** (Núi xa)

- **Kích thước**: 1920x1080 hoặc 2560x1440
- **Định dạng**: PNG (với alpha channel)
- **Màu sắc**: Xanh nhạt, xám xanh, hoặc tím nhạt (màu xa)
- **Nội dung**: Silhouette núi ở dưới ảnh (chiếm 40-60% chiều cao)
- **Lưu ý**:
  - Nên có nền trong suốt (transparent)
  - Màu đậm dần từ trên xuống dưới
  - Đỉnh núi nhọn hoặc tròn tùy style

**3. hills.png** (Đồi gần)

- **Kích thước**: 1920x1080 hoặc 2560x1440
- **Định dạng**: PNG (với alpha channel)
- **Màu sắc**: Xanh lá đậm (forest green, #228B22)
- **Nội dung**: Đồi ở dưới ảnh (chiếm 50-70% chiều cao)
- **Lưu ý**:
  - Phải có nền trong suốt (transparent)
  - Có thể có silhouette cây, bụi cây
  - Đồi nhấp nhô tự nhiên
  - Màu đậm hơn mountains

**4. ground.png** (Dải đất - QUAN TRỌNG)

- **Kích thước**: 512x512 (hoặc 256x256, 1024x1024)
- **Định dạng**: PNG
- **Màu sắc**: Nâu đất (#8B5A2B) + xanh cỏ (#228B22)
- **Nội dung**: Texture đất với cỏ ở trên
- **Lưu ý QUAN TRỌNG**:
  - **Phải tile được (seamless)**: Cạnh trái nối liền với cạnh phải
  - Cạnh trên nối liền với cạnh dưới
  - Sử dụng Photoshop Filter > Other > Offset để tạo seamless
  - Hoặc dùng tool online: [Seamless Texture Generator](https://www.imgonline.com.ua/eng/make-seamless-texture.php)
  - Phần trên (20-30%): Cỏ xanh
  - Phần dưới (70-80%): Đất nâu, có thể có đá, rễ cây

#### Cấu trúc thư mục:

```
app/src/main/assets/
  └── backgrounds/
      ├── sky.png          (1920x1080+)
      ├── mountains.png    (1920x1080+, transparent)
      ├── hills.png        (1920x1080+, transparent)
      └── ground.png       (512x512, seamless tileable)
```

#### Nguồn tải ảnh miễn phí:

**Backgrounds:**

- [OpenGameArt.org](https://opengameart.org/) - Tìm "parallax background"
- [itch.io](https://itch.io/game-assets/free) - Free game assets
- [Craftpix.net](https://craftpix.net/freebies/) - Free 2D game assets

**Seamless Textures:**

- [Textures.com](https://www.textures.com/) - Có bộ lọc "Seamless"
- [OpenGameArt.org](https://opengameart.org/art-search?keys=ground+texture)

**Tạo ảnh bằng AI:**

- DALL-E, Midjourney, Stable Diffusion
- Prompt: "pixel art parallax game background, sky layer, mountains, hills, seamless ground texture"

#### Ví dụ style phổ biến:

**Pixel Art Style:**

- Sky: 1920x1080, 8-bit màu xanh gradient
- Mountains: Pixelated silhouette
- Hills: Pixelated với pixel cây
- Ground: 256x256 pixel texture

**Cartoon Style:**

- Sky: Xanh sáng với mây trắng mềm
- Mountains: Vector style, màu flat
- Hills: Cây cối cartoon đơn giản
- Ground: Texture vẽ tay

**Realistic Style:**

- Sky: Photo-realistic gradient
- Mountains: Painted mountains với detail
- Hills: Trees với nhiều chi tiết
- Ground: High-res dirt/grass texture

#### Lưu ý quan trọng:

1. **Tỷ lệ aspect ratio**: Nên giữ 16:9 hoặc rộng hơn cho sky/mountains/hills
2. **Seamless ground**: Ground PHẢI tile được, nếu không sẽ thấy vết nối rõ ràng
3. **Transparent PNG**: mountains.png và hills.png nên có nền trong suốt
4. **File size**: Nén ảnh để giảm kích thước APK (dùng TinyPNG hoặc ImageOptim)
5. **Màu sắc nhất quán**: Các layer phải hài hòa về màu sắc
6. **Độ phân giải**: Với màn hình Full HD (1920x1080), nên dùng ảnh ít nhất 1920 rộng

#### Test ảnh trước khi dùng:

1. Mở ảnh ground.png trong Photoshop/GIMP
2. Duplicate layer và đặt cạnh nhau
3. Kiểm tra có vết nối rõ không
4. Nếu có, dùng Filter > Offset và chỉnh lại

---

### Nếu không có ảnh:

Game sẽ tự động tạo ảnh đơn giản bằng `BackgroundGenerator`:

- ✅ Sky: Xanh gradient + mây trắng
- ✅ Mountains: Núi xanh nhạt vector
- ✅ Hills: Đồi xanh lá nhấp nhô
- ✅ Ground: Đất nâu + cỏ xanh với texture đơn giản

Các ảnh này đủ để test game, nhưng không đẹp bằng ảnh thiết kế chuyên nghiệp.

## Tùy chỉnh

### Thay đổi vị trí ground:

```kotlin
// Trong GameMap.kt - init
groundY = screenHeight * 0.7f // 70% từ trên xuống
```

### Thay đổi tốc độ parallax:

```kotlin
// Trong GameMap.kt - init
mountainLayer = BackgroundLayer(
    context,
    "backgrounds/mountains.png",
    scrollSpeed = 0.3f, // Thay đổi giá trị này (0.0 - 1.0)
    screenHeight = screenHeight
)
```

### Thay đổi camera Y offset:

```kotlin
// Trong GameView.kt - update()
cameraY = groundY - height * 0.6f // Thay đổi 0.6f (0.0 - 1.0)
```

## Debug

Để debug ground line, bật dòng này trong `GameMap.kt - draw()`:

```kotlin
canvas.drawLine(0f, groundY - cameraY, screenWidth.toFloat(), groundY - cameraY, debugPaint)
```
