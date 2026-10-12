# Ss11ex3 - Tối ưu tốc độ CI bằng Gradle Dependency Caching

Bài 3 (Session 11): Bật tính năng caching cho Gradle dependencies trong GitHub
Actions để giảm thời gian tải thư viện từ Internet mỗi lần luồng CI chạy.

## Cấu trúc dự án

```
.
├── .github/workflows/ci.yml     # Workflow CI có cache: 'gradle'
├── gradle/wrapper/              # Gradle Wrapper 8.10.2
├── src/main/java/com/example/Calculator.java
├── src/test/java/com/example/CalculatorTest.java
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
└── report.md                    # Báo cáo so sánh thời gian chạy
```

## Điểm chính

Trong bước `actions/setup-java@v5`, tham số `cache: 'gradle'` được bổ sung:

```yaml
- name: Thiết lập môi trường Java JDK 17
  uses: actions/setup-java@v5
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'gradle'
```

Nhờ đó, Gradle dependencies (thư mục `~/.gradle/caches`) được lưu và khôi phục
giữa các lần chạy, giúp lần push thứ 2 nhanh hơn đáng kể.

## Chạy cục bộ

```bash
./gradlew test
```

Xem chi tiết kết quả so sánh tại [report.md](report.md).

## Lịch sử push kiểm tra cache

- Push #1: chưa có cache (cache miss) — job tải toàn bộ dependencies.
- Push #2: cache hit — Gradle dependencies được khôi phục từ GitHub Actions Cache.
