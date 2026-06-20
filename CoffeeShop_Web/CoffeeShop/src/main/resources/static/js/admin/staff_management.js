document.addEventListener("DOMContentLoaded", function () {
  const searchInput = document.getElementById("searchStaff");
  const tableBody = document.getElementById("staffTableBody");
  const staffForm = document.getElementById("staffForm");
  const staffIdInput = document.getElementById("staffId");
  const fullNameInput = document.getElementById("fullName");
  const phoneInput = document.getElementById("phone");
  const emailInput = document.getElementById("email");
  const usernameInput = document.getElementById("username");
  const accountPasswordInput = document.getElementById("accountPassword");
  const positionInput = document.getElementById("position");
  const statusInput = document.getElementById("status");

  // Xử lý tìm kiếm nhanh trên trang hiện tại
  if (searchInput && tableBody) {
    searchInput.addEventListener("input", function () {
      const filterText = searchInput.value.toLowerCase().trim();
      const rows = tableBody.getElementsByTagName("tr");

      for (let i = 0; i < rows.length; i++) {
        if (rows[i].cells.length <= 1) continue; // Bỏ qua dòng thông báo trống

        const nameCol = rows[i].cells[1]
          ? rows[i].cells[1].textContent.toLowerCase()
          : "";
        const emailCol = rows[i].cells[2]
          ? rows[i].cells[2].textContent.toLowerCase()
          : "";
        const phoneCol = rows[i].cells[3]
          ? rows[i].cells[3].textContent.toLowerCase()
          : "";

        if (
          nameCol.includes(filterText) ||
          emailCol.includes(filterText) ||
          phoneCol.includes(filterText)
        ) {
          rows[i].style.display = "";
        } else {
          rows[i].style.display = "none";
        }
      }
    });
  }

  // Xử lý thêm/sửa nhân viên
  if (staffForm) {
    staffForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      const payload = {
        fullName: fullNameInput ? fullNameInput.value : "",
        phone: phoneInput ? phoneInput.value : "",
        email: emailInput ? emailInput.value : "",
        username: usernameInput ? usernameInput.value : "",
        password: accountPasswordInput ? accountPasswordInput.value : "",
        position: positionInput ? positionInput.value : "",
        status: statusInput ? statusInput.value : "",
      };

      const staffId = staffIdInput ? staffIdInput.value.trim() : "";
      const isUpdate = !!staffId;
      const url = isUpdate
        ? "/admin/api/staff/" + encodeURIComponent(staffId)
        : "/admin/api/staff";
      const method = isUpdate ? "PUT" : "POST";

      try {
        const res = await fetch(url, {
          method,
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
        });

        const data = await res.json().catch(() => null);

        if (!res.ok) {
          showError(
            (data && data.message) ||
              (isUpdate
                ? "Cập nhật nhân viên thất bại!"
                : "Thêm nhân viên thất bại!"),
          );
          return;
        }

        const username = data && data.username ? data.username : "";

        showSuccess(
          (isUpdate ? "Cập nhật thành công. " : "Thêm nhân sự thành công. ") +
            (username ? "Tài khoản: " + username : ""),
        );

        // Đợi 1.5 giây cho thông báo chạy xong rồi mới load lại trang
        setTimeout(() => window.location.reload(), 1500);
      } catch (err) {
        console.error(err);
        showError("Không thể kết nối đến máy chủ!");
      }
    });
  }

  // Xử lý sự kiện click cho nút sửa và xóa (Sử dụng Event Delegation)
  if (tableBody) {
    tableBody.addEventListener("click", function (e) {
      // Tìm nút sửa hoặc xóa gần nhất với phần tử được click
      const editBtn = e.target.closest(".edit-btn");
      const deleteBtn = e.target.closest(".delete-btn");

      if (editBtn) {
        const staffId = editBtn.getAttribute("data-id");
        editStaff(staffId);
      } else if (deleteBtn) {
        const staffId = deleteBtn.getAttribute("data-id");
        deleteStaff(staffId);
      }
    });
  }
});

// Hàm lấy dữ liệu từ Controller đổ vào form sửa
async function editStaff(staffId) {
  const id = (staffId || "").trim();
  if (!id) return;

  const staffIdInput = document.getElementById("staffId");
  const fullNameInput = document.getElementById("fullName");
  const phoneInput = document.getElementById("phone");
  const emailInput = document.getElementById("email");
  const usernameInput = document.getElementById("username");
  const accountPasswordInput = document.getElementById("accountPassword");
  const positionInput = document.getElementById("position");
  const statusInput = document.getElementById("status");
  const formTitle = document.getElementById("formTitle");

  try {
    // ĐÂY LÀ ĐOẠN FETCH GỌI API CONTROLLER (Rất quan trọng, không được mất)
    const res = await fetch("/admin/api/staff/" + encodeURIComponent(id), {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
    });

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      showError((data && data.message) || "Không thể tải dữ liệu nhân viên!");
      return;
    }

    // ĐỔ DỮ LIỆU TỪ CONTROLLER VÀO FORM
    // (Dùng fallback dự phòng để khớp 100% mọi cấu trúc từ Java trả về)
    if (staffIdInput) staffIdInput.value = data.staffId || data.id || id;
    if (fullNameInput) fullNameInput.value = data.fullName || data.name || "";
    if (phoneInput) phoneInput.value = data.phone || "";
    if (emailInput) emailInput.value = data.email || "";
    if (usernameInput) usernameInput.value = data.username || "";

    // XỬ LÝ Ô MẬT KHẨU: Gỡ bỏ bắt buộc nhập khi ĐANG SỬA
    if (accountPasswordInput) {
      accountPasswordInput.value = "";
      accountPasswordInput.removeAttribute("required");
      accountPasswordInput.placeholder = "Bỏ trống nếu giữ nguyên mật khẩu cũ";
    }

    if (positionInput) positionInput.value = data.position || "";
    if (statusInput) statusInput.value = data.status || "";

    if (formTitle) formTitle.textContent = "Cập nhật Nhân Viên";

    // Cuộn lên form và focus vào ô tên
    if (fullNameInput) {
      fullNameInput.focus();
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  } catch (err) {
    console.error(err);
    showError("Không thể kết nối máy chủ để lấy dữ liệu!");
  }
}

// Hàm Xóa nhân viên
function deleteStaff(staffId) {
  const id = (staffId || "").trim();
  if (!id) return;

  // Gọi Hộp thoại Xác nhận (Custom Confirm)
  showConfirm(
    "Xóa nhân sự",
    `Bạn có chắc chắn muốn xóa nhân sự ${id} không?`,
    () => {
      // Nếu bấm Đồng ý thì đoạn code này sẽ chạy
      fetch("/admin/api/staff/" + encodeURIComponent(id), {
        method: "DELETE",
        headers: {
          Accept: "application/json",
        },
      })
        .then(async (res) => {
          let data = null;
          try {
            data = await res.json();
          } catch {
            data = null;
          }

          if (!res.ok) {
            showError((data && data.message) || "Xóa nhân viên thất bại!");
            return;
          }

          showSuccess("Đã xóa nhân viên khỏi hệ thống.");
          // Đợi thông báo biến mất rồi tải lại danh sách
          setTimeout(() => window.location.reload(), 1500);
        })
        .catch((err) => {
          console.error(err);
          showError("Không thể kết nối đến máy chủ!");
        });
    },
  );
}
