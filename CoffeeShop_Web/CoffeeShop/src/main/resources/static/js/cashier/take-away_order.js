// Khởi tạo mảng lưu trữ giỏ hàng (chứa các món khách chọn)
let cart = [];

document.addEventListener("DOMContentLoaded", function () {
  console.log("Dữ liệu thực đơn thật:", realMenuItems);
  console.log("Dữ liệu các bàn:", realTables);

  renderMenuGrid(realMenuItems);
  renderCart();
  setupSearch();
  loadTableSelect();

  // TỰ ĐỘNG KHỚP LINK TỪ SƠ ĐỒ BÀN TRUYỀN SANG (Ví dụ: ?table=1)
  const urlParams = new URLSearchParams(window.location.search);
  const tableParam = urlParams.get("table");
  if (tableParam) {
    if (typeof realTables !== "undefined" && realTables.length > 0) {
      const sortedTables = realTables.sort(
        (a, b) => a.tableNumber - b.tableNumber,
      );
      const targetTable = sortedTables.find(
        (t) => t.tableNumber === parseInt(tableParam),
      );
      if (targetTable) {
        // 1. Tự động chọn đúng số bàn tương ứng
        document.getElementById("takeawayTableSelect").value = targetTable.id;
        // 2. Tự động lật nút lựa chọn sang "Thêm món cho bàn"
        document.getElementById("radioAddTable").checked = true;
        toggleOrderTypeMode();
      }
    }
  }
});

// 0. Hàm đổ danh sách bàn thật vào ô Select (Giữ Bàn 0 làm mặc định)
function loadTableSelect() {
  const selectEl = document.getElementById("takeawayTableSelect");
  if (!selectEl) return;

  if (typeof realTables !== "undefined" && realTables.length > 0) {
    const sortedTables = realTables.sort(
      (a, b) => a.tableNumber - b.tableNumber,
    );
    sortedTables.forEach((table) => {
      // Đã có Bàn 0 cứng ở HTML rồi nên bỏ qua nếu CSDL vô tình có bàn 0
      if (table.tableNumber !== 0) {
        const option = document.createElement("option");
        option.value = table.id;
        option.textContent = `Bàn số ${table.tableNumber}`;
        selectEl.appendChild(option);
      }
    });
  }
}

// HÀM LẬT CHUYỂN ĐỔI CHẾ ĐỘ NÚT BẤM (Thanh toán vs YC Pha chế)
function toggleOrderTypeMode() {
  const isAddTableMode = document.getElementById("radioAddTable").checked;
  const actionBtn = document.getElementById("btnMainActionTakeaway");
  const selectTable = document.getElementById("takeawayTableSelect");

  if (isAddTableMode) {
    // Lật sang chế độ Yêu cầu pha chế cho bàn sảnh (Nút xanh dương)
    actionBtn.className = "btn btn-primary btn-lg w-100 py-3 fw-bold shadow-sm";
    actionBtn.innerHTML = `<i class="bx bx-paper-plane fs-4 mb-1 d-block"></i> YC PHA CHẾ`;

    // Nếu đang ở chế độ thêm món cho bàn mà select box vẫn giữ Bàn 0 -> Tự động nhảy xuống Bàn số 1 cho đỡ lỗi logic
    if (selectTable.value === "TABLE0" && selectTable.options.length > 1) {
      selectTable.selectedIndex = 1;
    }
  } else {
    // Quay về chế độ lập hóa đơn mang đi độc lập ban đầu (Nút xanh lá)
    actionBtn.className = "btn btn-success btn-lg w-100 py-3 fw-bold shadow-sm";
    actionBtn.innerHTML = `<i class="bx bx-check-circle fs-4 mb-1 d-block"></i> THANH TOÁN`;
    selectTable.value = "TABLE0";
  }
}

// HÀM ĐIỀU PHỐI HÀNH ĐỘNG KHI CLICK NÚT CHÍNH
function executeMainAction() {
  const isAddTableMode = document.getElementById("radioAddTable").checked;
  if (isAddTableMode) {
    sendItemsToKitchen(); // Chạy luồng gửi bếp (chuẩn bị)
  } else {
    checkoutTakeawayDirect(); // Chạy luồng tính tiền mang đi luôn
  }
}

// LUỒNG NGHIỆP VỤ 1: THÊM MÓN VÀO BÀN VÀ GỬI XUỐNG PHA CHẾ (PREPARING)
async function sendItemsToKitchen() {
  if (cart.length === 0) {
    showError("Vui lòng chọn món nước trước khi yêu cầu pha chế!");
    return;
  }

  const targetTableId = document.getElementById("takeawayTableSelect").value;
  if (targetTableId === "TABLE0") {
    showError("Vui lòng chọn một bàn thực tế để gửi món xuống bếp!");
    return;
  }

  const checkoutBtn = document.getElementById("btnMainActionTakeaway");
  checkoutBtn.disabled = true;

  // Lấy dữ liệu danh sách nước hiện tại của bàn đó trong bộ nhớ để gộp món lũy tiến
  const tableData = realTables.find((t) => t.id === targetTableId);
  let finalItems =
    tableData && tableData.currentOrder && tableData.currentOrder.items
      ? tableData.currentOrder.items
      : [];

  // Tạo cấu trúc danh sách nước mới (ĐÃ SỬA CHỮA CHÍ MẠNG Ở ĐÂY)
  cart.forEach((c) => {
    const newItem = {
      itemId: c.id, // <-- Bơm thêm dòng này cho Spring Boot (Java) đọc
      item_id: c.id, // <-- Firebase đọc
      itemName: c.name, // <-- Bơm thêm dòng này cho Spring Boot (Java) đọc
      item_name: c.name, // <-- Firebase đọc
      price: c.price,
      quantity: c.quantity,
      served: false, // Thiết lập là false để bếp nhận lệnh pha chế
      timestamp: "",
    };

    // Kiểm tra trùng món nước thì cộng dồn số lượng
    const duplicate = finalItems.find(
      (fi) => fi.item_id === c.id || fi.itemId === c.id,
    );
    if (duplicate) {
      duplicate.quantity += c.quantity;
    } else {
      finalItems.push(newItem);
    }
  });

  const payload = {
    items: finalItems,
    notes: "Thu ngân thêm món trực tiếp tại quầy POS",
  };

  try {
    // Bắn lệnh lên Rest API đồng bộ bàn để lật status bàn sang "preparing" rực ánh đèn xanh dương
    const response = await fetch(`/api/tables/${targetTableId}/sync-order`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (response.ok) {
      showSuccess("Đã gửi yêu cầu pha chế xuống quầy Bar thành công!");
      cart = [];
      renderCart();
      // Chờ 1.5s để xem thông báo rồi đưa thu ngân về lại trang sơ đồ bàn
      setTimeout(() => {
        window.location.href = "/cashier/dashboardCashier";
      }, 1500);
    } else {
      showError("Đồng bộ bếp thất bại!");
    }
  } catch (e) {
    console.error(e);
    showError("Lỗi kết nối mạng!");
  } finally {
    checkoutBtn.disabled = false;
    toggleOrderTypeMode();
  }
}

// LUỒNG NGHIỆP VỤ 2: THANH TOÁN LUÔN MANG VỀ ĐỘC LẬP
async function checkoutTakeawayDirect() {
  if (cart.length === 0) {
    showError("Vui lòng chọn món trước khi thanh toán!");
    return;
  }

  const checkoutBtn = document.getElementById("btnMainActionTakeaway");
  const originalBtnHtml = checkoutBtn.innerHTML;
  checkoutBtn.innerHTML =
    "<i class='bx bx-loader-alt bx-spin fs-4 mb-1 d-block'></i> ĐANG XỬ LÝ...";
  checkoutBtn.disabled = true;

  const totalAmount = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0,
  );

  const payload = {
    targetTableId: document.getElementById("takeawayTableSelect").value,
    items: cart,
    totalAmount: totalAmount,
  };

  try {
    const response = await fetch("/cashier/api/checkout", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    const data = await response.json();

    if (response.ok) {
      showSuccess(`Thanh toán mang về thành công! Mã HĐ: ${data.invoiceId}`);
      cart = [];
      renderCart();
    } else {
      showError("Thất bại: " + data.message);
    }
  } catch (error) {
    console.error(error);
    showError("Lỗi kết nối máy chủ!");
  } finally {
    checkoutBtn.innerHTML = originalBtnHtml;
    checkoutBtn.disabled = false;
  }
}

// CÁC HÀM XỬ LÝ UI: RENDER, THÊM XÓA SỬA GIỎ HÀNG
function renderMenuGrid(itemsToRender) {
  const gridContainer = document.getElementById("menuGridContainer");
  gridContainer.innerHTML = "";
  itemsToRender.forEach((item) => {
    const imageUrl = item.imageUrl
      ? item.imageUrl
      : "https://placehold.co/150x150/d4a574/fff?text=No+Image";
    const cardHtml = `
      <div class="menu-item-card" onclick="addToCart('${item.id}')">
        <img src="${imageUrl}" alt="${item.name}" class="menu-item-img">
        <div class="menu-item-name" title="${item.name}">${item.name}</div>
        <div class="menu-item-price">${formatCurrency(item.price)}</div>
      </div>
    `;
    gridContainer.insertAdjacentHTML("beforeend", cardHtml);
  });
}

function addToCart(itemId) {
  const product = realMenuItems.find((p) => p.id === itemId);
  if (!product) return;
  const existingItem = cart.find((item) => item.id === itemId);
  if (existingItem) {
    existingItem.quantity += 1;
  } else {
    cart.push({
      id: product.id,
      name: product.name,
      price: product.price,
      quantity: 1,
    });
  }
  renderCart();
}

function removeFromCart(itemId) {
  cart = cart.filter((item) => item.id !== itemId);
  renderCart();
}

function renderCart() {
  const cartBody = document.getElementById("cartTableBody");
  const totalAmountEl = document.getElementById("cartTotalAmount");
  cartBody.innerHTML = "";

  if (cart.length === 0) {
    cartBody.innerHTML = `<tr><td colspan="5" class="text-muted py-4">Chưa có món nào</td></tr>`;
    totalAmountEl.innerText = "0 đ";
    return;
  }

  let totalAmount = 0;
  cart.forEach((item, index) => {
    const itemTotal = item.price * item.quantity;
    totalAmount += itemTotal;
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${index + 1}</td>
      <td style="text-align: left; font-weight: 500;">${item.name}</td>
      <td><span class="badge bg-secondary">${item.quantity}</span></td>
      <td style="color: #d4a574; font-weight: bold;">${formatCurrency(itemTotal)}</td>
      <td>
        <button class="btn-remove-item border-0 bg-transparent text-danger fs-5" onclick="removeFromCart('${item.id}')">
          <i class='bx bx-x-circle'></i>
        </button>
      </td>
    `;
    cartBody.appendChild(tr);
  });
  totalAmountEl.innerText = formatCurrency(totalAmount);
}

function setupSearch() {
  const searchInput = document.getElementById("searchMenuInput");
  if (searchInput) {
    searchInput.addEventListener("input", function () {
      const keyword = this.value.toLowerCase().trim();
      const filteredItems = realMenuItems.filter((item) =>
        item.name.toLowerCase().includes(keyword),
      );
      renderMenuGrid(filteredItems);
    });
  }
}

// Sửa hàm clearCart để dùng showConfirm
function clearCart() {
  if (cart.length > 0) {
    showConfirm("Hủy đơn", "Bạn có chắc chắn muốn huỷ đơn hiện tại?", () => {
      cart = [];
      document.getElementById("radioTakeaway").checked = true;
      toggleOrderTypeMode();
      renderCart();
      showSuccess("Đã hủy đơn hiện hành.");
    });
  }
}

function formatCurrency(amount) {
  return amount.toLocaleString("vi-VN") + " đ";
}
