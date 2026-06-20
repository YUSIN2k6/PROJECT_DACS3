document.addEventListener("DOMContentLoaded", function () {
  loadProductSelect();
  attachEventListeners();
});

function loadProductSelect() {
  const selectEl = document.getElementById("productSelect");
  selectEl.innerHTML = '<option value="">-- Chọn nước --</option>';

  rawMenuItems.forEach((item) => {
    const cat = rawCategories.find((c) => c.id === item.categoryId);
    const catName = cat ? cat.name : "Khác";
    // Đổi lại dùng active/inactive chuẩn của hệ thống
    const statusVal = item.status === "available" ? "available" : "unavailable";

    const option = document.createElement("option");
    option.value = item.id;
    option.textContent = `${item.name} (${catName})`;
    option.dataset.status = statusVal;
    selectEl.appendChild(option);
  });
}

function attachEventListeners() {
  document
    .getElementById("searchInput")
    .addEventListener("input", filterInventory);
  document
    .getElementById("filterStatus")
    .addEventListener("change", filterInventory);

  document
    .getElementById("productSelect")
    .addEventListener("change", function () {
      if (this.value) {
        const selectedOption = this.options[this.selectedIndex];
        const status = selectedOption.dataset.status;
        const radioBtn = document.querySelector(
          `input[name="status"][value="${status}"]`,
        );
        if (radioBtn) radioBtn.checked = true;
      }
    });

  document
    .getElementById("updateBtn")
    .addEventListener("click", updateProductStatus);
}

function filterInventory() {
  const searchVal = document
    .getElementById("searchInput")
    .value.toLowerCase()
    .trim();
  const filterVal = document.getElementById("filterStatus").value;
  const rows = document.querySelectorAll(
    "#inventoryTableBody tr:not(#noDataRow)",
  );

  rows.forEach((row) => {
    const nameCol = row.cells[1]?.textContent.toLowerCase() || "";
    const catCol = row.cells[2]?.textContent.toLowerCase() || "";
    const statusBadge = row.querySelector(".status-badge");
    // Kiểm tra theo class của UI
    const isAvailable =
      statusBadge && statusBadge.classList.contains("status-available");
    const currentStatus = isAvailable ? "available" : "unavailable";

    const matchSearch =
      nameCol.includes(searchVal) || catCol.includes(searchVal);
    const matchStatus = filterVal === "all" || currentStatus === filterVal;

    row.style.display = matchSearch && matchStatus ? "" : "none";
  });
}

async function updateProductStatus() {
  const selectedProductId = document.getElementById("productSelect").value;
  const checkedRadio = document.querySelector('input[name="status"]:checked');

  if (!selectedProductId) {
    showError("Vui lòng chọn nước cần cập nhật!");
    return;
  }

  if (!checkedRadio) {
    showError("Vui lòng chọn trạng thái (Còn hàng / Hết hàng)!");
    return;
  }

  // uiStatus lúc này lấy thẳng giá trị value="active" hoặc "inactive" từ radio HTML
  const dbStatus = checkedRadio.value;

  const updateBtn = document.getElementById("updateBtn");
  updateBtn.innerHTML =
    "<i class='bx bx-loader-alt bx-spin'></i> Đang cập nhật...";
  updateBtn.disabled = true;

  try {
    const response = await fetch(
      `/cashier/api/inventory/${selectedProductId}/status`,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: dbStatus }),
      },
    );

    const data = await response.json().catch(() => null);

    if (response.ok) {
      showSuccess("Cập nhật trạng thái kho thành công!");
      // Chờ 1.5 giây để thu ngân xem thông báo rồi mới nạp lại trang
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    } else {
      showError(
        "Lỗi: " + (data && data.message ? data.message : "Cập nhật thất bại"),
      );
    }
  } catch (error) {
    console.error(error);
    showError("Không thể kết nối đến máy chủ!");
  } finally {
    updateBtn.innerHTML = "<i class='bx bxs-save'></i> Cập Nhật";
    updateBtn.disabled = false;
  }
}
