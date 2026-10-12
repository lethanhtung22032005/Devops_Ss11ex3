# Báo cáo: Tối ưu tốc độ CI bằng Gradle Dependency Caching

## 1. Mục tiêu

Bật caching cho Gradle dependencies trong GitHub Actions để không phải tải lại
toàn bộ thư viện từ Internet ở mỗi lần chạy CI.

## 2. Thay đổi thực hiện

Bổ sung `cache: 'gradle'` vào bước `actions/setup-java@v5` trong
`.github/workflows/ci.yml`:

```yaml
- name: Thiết lập môi trường Java JDK 17
  uses: actions/setup-java@v5
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'gradle'
```

Khi bật tùy chọn này, action sẽ:
- Tự động băm (hash) các tệp `**/*.gradle*` và `**/gradle-wrapper.properties`
  để tạo cache key dạng `gradle-<hash>`.
- Lưu thư mục `~/.gradle/caches` và `~/.gradle/wrapper` vào GitHub Actions Cache.
- Ở các lần chạy sau, nếu cache key trùng thì khôi phục lại cache thay vì tải mới.

## 3. Kết quả đo thời gian (lấy từ tab Actions)

Thời gian đo trực tiếp từ log các lần chạy:

| Lần chạy | Trạng thái cache | Bước `setup-java` | Bước `./gradlew test` | Tổng thời gian Job |
|----------|------------------|-------------------|------------------------|--------------------|
| Run #1 – push đầu | Cache miss | ~1s | ~16s | 26s (tổng run 29s) |
| Run #2 – push ngay sau | Cache miss (chạy song song run #1 nên chưa kịp lưu cache) | ~1s | ~17s | 23s (tổng run 25s) |
| Run #3 – push sau khi cache đã lưu | **Cache hit** | ~2s (gồm khôi phục cache) | ~7s | 15s (tổng run 18s) |

Chênh lệch rõ nhất ở bước `./gradlew test`: **~16s → ~7s** (giảm hơn 50%) và
tổng thời gian job **26s → 15s**, do không phải tải lại dependencies từ Maven Central.

> Lần push #2 chạy chỉ ~6 giây sau push #1 (cả hai workflow chạy song song), nên
> cache chưa kịp lưu → vẫn cache miss. Push #3 chạy sau khi cache đã lưu nên đạt cache hit.

## 4. Kiểm tra log lần push có cache

Trong log của bước `setup-java`, xuất hiện dòng xác nhận khôi phục cache:

```
Cache restored successfully from key: gradle-<hash>
```

Hoặc:

```
Received <N> MB over the network, <M> MB from cache
Cache restored from key: gradle-<hash>
```

Nếu là lần đầu tiên, log sẽ hiển thị `Cache not found for input keys: gradle-<hash>`
và cuối Job sẽ có bước `Post Setup Java` để lưu cache mới.

## 5. Kết luận

- Sau khi cache được lưu, các lần push tiếp theo nhanh hơn rõ rệt: bước
  `./gradlew test` giảm từ ~16s xuống ~7s, tổng thời gian job giảm từ 26s xuống 15s.
- Bước `setup-java` thể hiện việc khôi phục cache thay vì tải mới.
- Bước `./gradlew test` giảm mạnh vì không phải resolve và download thư viện
  (JUnit 5, Gradle plugins...) từ Maven Central.
- Cơ chế cache tự động vô hiệu hóa (invalidate) khi các tệp cấu hình Gradle
  thay đổi, nên vẫn đảm bảo tính đúng đắn của build.
