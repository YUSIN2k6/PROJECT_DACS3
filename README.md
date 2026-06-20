# CoffeeShop Management System

Hệ thống quản lý quán cà phê gồm Website (Spring Boot + Thymeleaf) và Ứng dụng Android (Kotlin + Jetpack Compose), sử dụng Firebase Realtime Database đồng bộ dữ liệu và Supabase Storage lưu trữ ảnh.

## 📋 Yêu cầu môi trường

Trước khi mở dự án, bạn cần cài đặt các phần mềm sau:

1. **JDK 17 hoặc cao hơn**: [Tải JDK Oracle](https://www.oracle.com/java/technologies/downloads/) hoặc [OpenJDK](https://adoptium.net/)
2. **IntelliJ IDEA (Ultimate hoặc Community)**: [Tải IntelliJ](https://www.jetbrains.com/idea/download/)
3. **Android Studio**: [Tải Android Studio](https://developer.android.com/studio)
4. **Git**: [Tải Git](https://git-scm.com/downloads)

## 🚀 Bước 1: Cấu hình Firebase Realtime Database

Dự án sử dụng Firebase để đồng bộ dữ liệu real-time. Bạn cần lấy tệp cấu hình từ Firebase Console:

1. Đăng nhập [Firebase Console](https://console.firebase.google.com/)
2. Chọn dự án CoffeeShop của bạn (hoặc tạo mới nếu chưa có)
3. **Cho Website (Spring Boot)**:
   - Vào **Project Settings** → **Service Accounts**
   - Nhấn **Generate New Private Key** → Lưu tệp thành `serviceAccountKey.json`
   - Đặt tệp này vào thư mục `CoffeeShop_Web/CoffeeShop/src/main/resources/`
4. **Cho Ứng dụng Android**:
   - Vào **Project Settings** → **Your apps** → Chọn ứng dụng Android
   - Nhấn **Download google-services.json**
   - Đặt tệp này vào thư mục `CoffeeShop_Moblie/app/`

## 🚀 Bước 2: Cấu hình Supabase Storage

Dự án sử dụng Supabase để lưu trữ ảnh món ăn:

1. Đăng nhập [Supabase Dashboard](https://supabase.com/dashboard)
2. Chọn dự án của bạn (hoặc tạo mới)
3. Tạo một bucket có tên `menu-images` (hoặc dùng bucket public mặc định)
4. Vào **Project Settings** → **API** → Lấy:
   - `Project URL` (ví dụ: `https://xyz.supabase.co`)
   - `anon public` (hoặc `service_role`) key
5. Mở file `application.properties` trong `CoffeeShop_Web/CoffeeShop/src/main/resources/` và cập nhật:

   ```properties
   supabase.url=YOUR_SUPABASE_PROJECT_URL
   supabase.key=YOUR_SUPABASE_ANON_KEY
   supabase.bucket=menu-images

   ## 🚀 Bước 3: Mở và chạy Website (Spring Boot)
   ```

6. Mở IntelliJ IDEA
7. Chọn File → Open → Chọn thư mục CoffeeShop_Web/CoffeeShop
8. Đợi IntelliJ tải dependencies Maven (có thể mất vài phút)
9. Mở class CoffeeShopApplication.java (thường ở src/main/java/com/example/CoffeeShop/ )
10. Nhấn nút Run (mũi tên xanh) cạnh tên class
11. Mở trình duyệt và truy cập: http://localhost:8080

## 🚀 Bước 4: Mở và chạy Ứng dụng Android

1. Mở Android Studio
2. Chọn File → Open → Chọn thư mục CoffeeShop_Moblie
3. Đợi Android Studio đồng bộ Gradle (có thể mất vài phút)
4. Kết nối điện thoại Android qua USB (hoặc mở Emulator Android)
5. Nhấn nút Run (mũi tên xanh) trên thanh công cụ
6. Ứng dụng sẽ được cài đặt và chạy trên thiết bị

- Các tài khoản truy cập

* Đăng nhập trang Quản trị viện: tk: admin , mk: Admin2006
* Đăng nhâp trang Thu ngân: tk: cashier1 , mk: Admin2006
* Đăng nhập điện thoại: tk: staff1 , mk: Admin2006
