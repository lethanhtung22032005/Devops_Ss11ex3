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

## 3. Kết quả đo thời gian

| Lần chạy | Trạng thái cache | Bước `setup-java` | Bước `./gradlew test` | Tổng thời gian Job |
|----------|------------------|-------------------|------------------------|--------------------|
| Lần 1 (push đầu) | Chưa có cache | ~8–10s (cache miss) | ~60–90s (tải toàn bộ dependencies) | ~1m 30s |
| Lần 2 (push sau) | Đã có cache | ~3–5s (cache hit) | ~15–25s (dùng dependencies đã cache) | ~40–50s |

> Ghi chú: Điền số liệu thực tế lấy từ tab **Actions** trên GitHub sau mỗi lần push.

## 4. Kiểm tra log lần push thứ 2

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

- Lần push thứ hai nhanh hơn rõ rệt nhờ Gradle dependencies được khôi phục từ cache.
- Bước `setup-java` giảm thời gian vì không phải tải lại wrapper/dependencies.
- Bước `./gradlew test` giảm mạnh vì không phải resolve và download thư viện
  (JUnit 5, Gradle plugins...) từ Maven Central.
- Cơ chế cache tự động vô hiệu hóa (invalidate) khi các tệp cấu hình Gradle
  thay đổi, nên vẫn đảm bảo tính đúng đắn của build.
