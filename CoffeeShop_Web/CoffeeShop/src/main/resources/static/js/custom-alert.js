// TỰ ĐỘNG BƠM MÃ HTML VÀO GIAO DIỆN KHI TẢI TRANG
document.addEventListener("DOMContentLoaded", () => {
  // 1. Vùng chứa thông báo nhỏ góc phải (Toast)
  const toastContainer = document.createElement("div");
  toastContainer.id = "customToastContainer";
  document.body.appendChild(toastContainer);

  // 2. Vùng chứa hộp thoại Xác nhận (Confirm Modal)
  const confirmHtml = `
        <div id="customConfirmOverlay">
            <div class="custom-confirm-box">
                <i class='bx bx-error-circle custom-confirm-icon'></i>
                <div class="custom-confirm-title" id="customConfirmTitle">Xác nhận</div>
                <div class="custom-confirm-text" id="customConfirmText">Bạn có chắc chắn muốn thực hiện hành động này?</div>
                <div class="custom-confirm-actions">
                    <button class="custom-btn custom-btn-cancel" id="customConfirmBtnCancel">Hủy bỏ</button>
                    <button class="custom-btn custom-btn-confirm" id="customConfirmBtnOk">Đồng ý</button>
                </div>
            </div>
        </div>
    `;
  document.body.insertAdjacentHTML("beforeend", confirmHtml);
});

// ==========================================
// HÀM HIỂN THỊ THÔNG BÁO NHỎ GÓC MÀN HÌNH
// ==========================================
function showToast(message, type) {
  const container = document.getElementById("customToastContainer");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = `custom-toast ${type}`;

  // Tận dụng thư viện Boxicons có sẵn trong dự án của bạn
  const iconClass = type === "success" ? "bx-check-circle" : "bx-x-circle";

  toast.innerHTML = `
        <i class='bx ${iconClass} custom-toast-icon'></i>
        <span class="custom-toast-msg">${message}</span>
    `;

  container.appendChild(toast);

  // Kích hoạt Animation trượt ra
  setTimeout(() => toast.classList.add("show"), 10);

  // Tự động tắt sau 3 giây
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 400); // Đợi CSS trượt vào xong rồi mới xóa node
  }, 3000);
}

function showSuccess(message) {
  showToast(message, "success");
}

function showError(message) {
  showToast(message, "error");
}

// ==========================================
// HÀM HIỂN THỊ HỘP THOẠI XÁC NHẬN BẤT ĐỒNG BỘ
// ==========================================
function showConfirm(title, text, callback) {
  const overlay = document.getElementById("customConfirmOverlay");
  document.getElementById("customConfirmTitle").textContent = title;
  document.getElementById("customConfirmText").textContent = text;

  const btnOk = document.getElementById("customConfirmBtnOk");
  const btnCancel = document.getElementById("customConfirmBtnCancel");

  // Mẹo chống lặp sự kiện: Clone nút để reset lại Listener cũ
  const newBtnOk = btnOk.cloneNode(true);
  btnOk.parentNode.replaceChild(newBtnOk, btnOk);

  const newBtnCancel = btnCancel.cloneNode(true);
  btnCancel.parentNode.replaceChild(newBtnCancel, btnCancel);

  // Hiển thị hộp thoại
  overlay.classList.add("show");

  // Xử lý khi ấn nút Hủy
  newBtnCancel.addEventListener("click", () => {
    overlay.classList.remove("show");
  });

  // Xử lý khi ấn nút Đồng ý
  newBtnOk.addEventListener("click", () => {
    overlay.classList.remove("show");
    callback(); // Gọi chạy logic nghiệp vụ
  });
}
