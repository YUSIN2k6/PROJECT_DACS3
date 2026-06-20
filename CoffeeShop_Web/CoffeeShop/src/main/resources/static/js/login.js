// ===== LOGIN PAGE FUNCTIONALITY (UI & REMEMBER ME ONLY) =====

document.addEventListener("DOMContentLoaded", function () {
  const loginForm = document.getElementById("loginForm");
  const togglePasswordBtn = document.getElementById("togglePassword");
  const passwordInput = document.getElementById("password");
  const rememberMeCheckbox = document.getElementById("rememberMe");

  // Tự động điền lại tài khoản nếu có lưu trước đó
  const rememberedUsername = localStorage.getItem("rememberedUsername");
  if (rememberedUsername) {
    document.getElementById("username").value = rememberedUsername;
    rememberMeCheckbox.checked = true;
  }

  // Bật/Tắt ẩn hiện mật khẩu (Hiệu ứng giao diện)
  togglePasswordBtn.addEventListener("click", function (e) {
    e.preventDefault();
    const type = passwordInput.type === "password" ? "text" : "password";
    passwordInput.type = type;

    // Đổi icon mắt
    const icon = this.querySelector("i");
    icon.classList.toggle("bx-hide");
    icon.classList.toggle("bx-show");
  });

  // Khi bấm nút Đăng nhập, lưu trạng thái ghi nhớ rồi gửi thẳng lên Server
  loginForm.addEventListener("submit", function () {
    const username = document.getElementById("username").value.trim();

    if (rememberMeCheckbox.checked) {
      localStorage.setItem("rememberedUsername", username);
    } else {
      localStorage.removeItem("rememberedUsername");
    }
    // Form tự động submit thẳng lên Java Controller, không bị chặn lại nữa
  });
});
