/**
 * Cashier Dashboard - Xử lý Sơ đồ bàn POS (Tối ưu UX Thao tác 1 chạm không cần Reload)
 */

let currentFilter = "all";
let selectedTableId = null;

document.addEventListener("DOMContentLoaded", function () {
  if (typeof tableList !== "undefined" && tableList != null) {
    tableList.sort((a, b) => a.tableNumber - b.tableNumber);
  } else {
    tableList = [];
  }

  const savedTableId = sessionStorage.getItem("pos_selected_table");
  if (savedTableId) {
    selectedTableId = savedTableId;
  }

  renderTableGrid();
  attachFilterEvents();

  if (selectedTableId) {
    const table = tableList.find((t) => t.id === selectedTableId);
    if (table) updateSidebar(table);
  }
});

// 1. RENDER LƯỚI SƠ ĐỒ BÀN THẬT TỪ FIREBASE
function renderTableGrid() {
  const grid = document.getElementById("tablesGrid");
  if (!grid) return;
  grid.innerHTML = "";

  const filteredTables = tableList.filter(
    (t) => currentFilter === "all" || t.status === currentFilter,
  );

  filteredTables.forEach((table) => {
    const isSelected = table.id === selectedTableId ? "selected" : "";
    const statusText = getStatusText(table.status);

    const activeOrder = table.currentOrder || table.current_order;
    let totalAmount = 0;

    if (activeOrder && activeOrder.items) {
      totalAmount = activeOrder.items.reduce(
        (sum, item) => sum + item.price * item.quantity,
        0,
      );
    }

    // ĐÃ SỬA: Lấy số bàn đích từ biến mergedWith (hoặc merged_with tùy theo JSON Firebase trả về)
    const targetMergeNum = table.mergedWith || table.merged_with || "";

    const displaySub =
      table.status === "merged"
        ? `Đang gộp ${targetMergeNum}`
        : table.status !== "available" && totalAmount > 0
          ? `${totalAmount.toLocaleString("vi-VN")}đ`
          : statusText;

    const cardHtml = `
      <div class="pos-table-card ${table.status} ${isSelected}" onclick="handleTableClick('${table.id}')">
        <div class="card-number">${table.tableNumber}</div>
        <div class="card-status">${displaySub}</div>
      </div>
    `;
    grid.insertAdjacentHTML("beforeend", cardHtml);
  });
}

function getStatusText(status) {
  const map = {
    available: "Trống",
    occupied: "Có khách",
    preparing: "Chuẩn bị",
    served: "Đã phục vụ",
    pending: "Chờ tính tiền",
    merged: "Đang gộp",
  };
  return map[status] || status;
}

function getStatusBadgeClass(status) {
  const map = {
    available: "bg-secondary",
    occupied: "bg-success",
    preparing: "bg-primary",
    served: "bg-warning text-dark",
    pending: "bg-danger",
    merged: "bg-info text-dark",
  };
  return map[status] || "bg-secondary";
}

// 2. XỬ LÝ LỌC TRẠNG THÁI THEO TAB TÌM KIẾM
function attachFilterEvents() {
  document.querySelectorAll(".filter-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
      document
        .querySelectorAll(".filter-btn")
        .forEach((b) => b.classList.remove("active"));
      this.classList.add("active");
      currentFilter = this.dataset.filter;
      renderTableGrid();
    });
  });
}

// 3. XỬ LÝ KHI CLICK CHỌN BÀN TRÊN LƯỚI POS
function handleTableClick(tableId) {
  selectedTableId = tableId;
  sessionStorage.setItem("pos_selected_table", tableId);
  renderTableGrid();

  const table = tableList.find((t) => t.id === tableId);
  if (table) {
    updateSidebar(table);
  }
}

// 4. CẬP NHẬT THANH SIDEBAR CHI TIẾT ĐƠN HÀNG BÊN PHẢI MÀN HÌNH
function updateSidebar(table) {
  document.getElementById("sidebarEmptyMessage").style.display = "none";
  document.getElementById("preparingTableContainer").style.display = "block";

  document.querySelector(".num-table").textContent = table.tableNumber;

  const statusBadge = document.getElementById("sidebarTableStatus");
  statusBadge.className = `badge ${getStatusBadgeClass(table.status)}`;
  statusBadge.textContent = getStatusText(table.status);

  const activeOrder = table.currentOrder || table.current_order;
  const items = activeOrder && activeOrder.items ? activeOrder.items : [];

  let totalAmount = 0;
  if (items.length > 0) {
    totalAmount = items.reduce(
      (sum, item) => sum + item.price * item.quantity,
      0,
    );
  }
  document.getElementById("sidebarTotalAmount").textContent =
    `${totalAmount.toLocaleString("vi-VN")} đ`;

  // === ĐOẠN CODE KHÓA NÚT CHỨC NĂNG ===
  const btnSwitch = document.getElementById("btnSwitchTable");
  const btnCancel = document.getElementById("btnCancelTable");
  const btnMerge = document.getElementById("btnMergeTable");
  if (table.status === "available") {
    btnSwitch.disabled = true;
    btnCancel.disabled = true;
    btnMerge.disabled = false;
  } else if (table.status === "merged") {
    btnSwitch.disabled = false;
    btnMerge.disabled = true;
    btnCancel.disabled = false;
  } else {
    btnSwitch.disabled = false;
    btnCancel.disabled = false;
    btnMerge.disabled = true;
  }

  // BIẾN ĐỔI NÚT CHỨC NĂNG THÔNG MINH DỰA TRÊN TRẠNG THÁI BÀN
  const btnCheckout = document.getElementById("btnCheckoutSidebar");

  if (table.status === "available") {
    btnCheckout.disabled = false;
    btnCheckout.innerHTML = `<i class="bx bx-user-plus"></i> CÓ KHÁCH`;
    btnCheckout.className = "btn btn-success w-100 py-3 fw-bold fs-5 shadow-sm";
    btnCheckout.onclick = function () {
      markTableOccupied(table.id);
    };
  } else if (table.status === "occupied") {
    btnCheckout.disabled = false;
    btnCheckout.innerHTML = `<i class="bx bx-coffee-togo"></i> CHỌN NƯỚC`;
    btnCheckout.className = "btn btn-primary w-100 py-3 fw-bold fs-5 shadow-sm";
    btnCheckout.onclick = function () {
      window.location.href = `/cashier/take-away_order?table=${table.tableNumber}`;
    };
  } else if (table.status === "preparing") {
    btnCheckout.disabled = true;
    btnCheckout.innerHTML = `<i class="bx bx-block"></i> CHƯA THỂ THANH TOÁN`;
    btnCheckout.className =
      "btn btn-secondary w-100 py-3 fw-bold fs-5 shadow-sm";
    btnCheckout.onclick = null;
  } else if (table.status === "served" || table.status === "pending") {
    btnCheckout.disabled = false;
    btnCheckout.innerHTML = `<i class="bx bx-credit-card-front"></i> THANH TOÁN`;
    btnCheckout.className = "btn btn-success w-100 py-3 fw-bold fs-5 shadow-sm";
    btnCheckout.onclick = function () {
      window.location.href = `/cashier/checkout?table=${table.tableNumber}`;
    };
  } else if (table.status === "merged") {
    btnCheckout.disabled = true;
    btnCheckout.innerHTML = `<i class="bx bx-lock-alt"></i> BÀN ĐANG GỘP`;
    btnCheckout.className =
      "btn btn-secondary w-100 py-3 fw-bold fs-5 shadow-sm";
    btnCheckout.onclick = null;
  }

  const listContainer = document.getElementById("preparingItemsList");
  listContainer.innerHTML = "";

  if (items.length > 0) {
    items.forEach((item) => {
      const nameOfProduct = item.item_name || item.itemName;
      const idOfProduct = item.item_id || item.itemId;

      const actionHtml = item.served
        ? `<span class="item-completed-text"><i class="bx bx-check-double text-success"></i> Đã ra món</span>`
        : `<div class="d-flex align-items-center gap-1 justify-content-end">
             <button class="btn btn-sm btn-outline-danger px-2 py-1 border-0" onclick="cancelOrderItem('${table.id}', '${idOfProduct}')" title="Bỏ món này">
               <i class="bx bx-trash fs-5"></i>
             </button>
             <button class="btn btn-sm btn-done-item px-3" onclick="markItemServed('${table.id}', '${idOfProduct}')"><i class="bx bx-check"></i> Xong</button>
           </div>`;

      const rowHtml = `
        <div class="order-item-row">
            <div class="item-info">
                <div class="item-name">${nameOfProduct}</div>
                <div class="item-qty text-muted small">Số lượng: x${item.quantity}</div>
            </div>
            <div class="item-action">
                ${actionHtml}
            </div>
        </div>
      `;
      listContainer.insertAdjacentHTML("beforeend", rowHtml);
    });

    document.getElementById("preparingNotes").textContent =
      activeOrder.notes || "Không có ghi chú";
    document.querySelector(".note-box").style.display = "block";
  } else {
    listContainer.innerHTML = `<div class="text-center text-muted py-3">Bàn này chưa gọi món nước nào</div>`;
    document.querySelector(".note-box").style.display = "none";
  }
}

// 5. THAO TÁC: PHA CHẾ BẤM "XONG" TỪNG MÓN NƯỚC
function markItemServed(tableId, itemId) {
  const table = tableList.find((t) => t.id === tableId);
  const activeOrder = table ? table.currentOrder || table.current_order : null;
  if (!table || !activeOrder || !activeOrder.items) return;

  const item = activeOrder.items.find(
    (i) => i.item_id === itemId || i.itemId === itemId,
  );
  if (item) {
    item.served = true;
    const now = new Date();
    item.timestamp = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
  }

  const isAllServed = activeOrder.items.every((i) => i.served === true);
  if (isAllServed && table.status === "preparing") {
    table.status = "served";
  }

  fetch(`/api/tables/${tableId}/sync-order`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(activeOrder),
  })
    .then(() => console.log("Đã đồng bộ món nước phục vụ lên Firebase!"))
    .catch((e) => console.error("Lỗi đồng bộ mạng: ", e));

  renderTableGrid();
  updateSidebar(table);
}

// 6. THAO TÁC MỚI: BỎ (HỦY) MÓN TRONG LÚC ĐANG CHUẨN BỊ
function cancelOrderItem(tableId, itemId) {
  const table = tableList.find((t) => t.id === tableId);
  const activeOrder = table ? table.currentOrder || table.current_order : null;
  if (!table || !activeOrder || !activeOrder.items) return;

  const itemToRemove = activeOrder.items.find(
    (i) => i.item_id === itemId || i.itemId === itemId,
  );
  if (!itemToRemove) return;

  const itemName = itemToRemove.item_name || itemToRemove.itemName || "món này";

  // Hàm xử lý chung lưu Firebase sau khi đã thao tác (để tái sử dụng trong callback)
  const processCancelLogic = () => {
    if (activeOrder.items.length === 0) {
      showSuccess("Bàn đã bị hủy hết nước! Sơ đồ sẽ chuyển về 'Có khách'.");
      table.status = "occupied";
    } else {
      const isAllServed = activeOrder.items.every((i) => i.served === true);
      if (isAllServed && table.status === "preparing") {
        table.status = "served";
      } else {
        table.status = "preparing";
      }
    }

    fetch(`/api/tables/${tableId}/sync-order`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(activeOrder),
    })
      .then(() => {
        setTimeout(() => window.location.reload(), 1500); // Đợi 1.5s xem thông báo
      })
      .catch((e) => {
        console.error("Lỗi đồng bộ mạng: ", e);
        showError("Lỗi kết nối máy chủ!");
      });
  };

  if (itemToRemove.quantity > 1) {
    showConfirm(
      "Giảm số lượng",
      `Bàn đang gọi ${itemToRemove.quantity} ly "${itemName}". Bạn có muốn GIẢM bớt 1 ly không?`,
      () => {
        itemToRemove.quantity -= 1;
        showSuccess(`Đã giảm 1 ly ${itemName}`);
        processCancelLogic();
      },
    );
  } else {
    showConfirm(
      "Hủy món",
      `Bạn có chắc chắn muốn HỦY hoàn toàn món "${itemName}" khỏi đơn không?`,
      () => {
        activeOrder.items = activeOrder.items.filter(
          (i) => i.item_id !== itemId && i.itemId !== itemId,
        );
        showSuccess(`Đã hủy món ${itemName}`);
        processCancelLogic();
      },
    );
  }
}

// 7. ĐIỀU HƯỚNG SANG TRANG KHÁC
function handleTakeawayOrder() {
  window.location.href = `/cashier/take-away_order`;
}

function handlePaymentFromSidebar() {
  if (!selectedTableId) return;
  const table = tableList.find((t) => t.id === selectedTableId);
  window.location.href = `/cashier/checkout?table=${table.tableNumber}`;
}

// HÀM LẬT TRẠNG THÁI BÀN SANG CÓ KHÁCH KHÔNG CẦN CHỜ REFRESH
function markTableOccupied(tableId) {
  const table = tableList.find((t) => t.id === tableId);
  if (!table) return;

  const emptyOrder = { items: [], notes: "" };

  fetch(`/api/tables/${tableId}/sync-order`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(emptyOrder),
  })
    .then(() => {
      window.location.reload();
    })
    .catch((e) => console.error("Lỗi đồng bộ mạng: ", e));
}

// 8. TÍNH NĂNG ĐỔI BÀN (SWITCH TABLE)
let switchModalInstance = null;

function openSwitchTableModal() {
  if (!selectedTableId) return;

  const currentTable = tableList.find((t) => t.id === selectedTableId);
  if (!currentTable || currentTable.status === "available") {
    showError("Bàn đang trống, không thể đổi!");
    return;
  }

  const selectEl = document.getElementById("targetTableSelect");
  selectEl.innerHTML = "";

  const availableTables = tableList
    .filter((t) => t.status === "available")
    .sort((a, b) => a.tableNumber - b.tableNumber);

  if (availableTables.length === 0) {
    selectEl.innerHTML = `<option value="">-- Quán đã hết bàn trống --</option>`;
    selectEl.disabled = true;
  } else {
    selectEl.disabled = false;
    availableTables.forEach((t) => {
      const option = document.createElement("option");
      option.value = t.id;
      option.textContent = `Bàn số ${t.tableNumber}`;
      selectEl.appendChild(option);
    });
  }

  const modalEl = document.getElementById("switchTableModal");
  switchModalInstance = new bootstrap.Modal(modalEl);
  switchModalInstance.show();
}

function confirmSwitchTable() {
  const targetTableId = document.getElementById("targetTableSelect").value;
  if (!targetTableId) {
    showError("Vui lòng chọn một bàn đích hợp lệ!");
    return;
  }

  showConfirm("Đổi bàn", "Xác nhận chuyển dữ liệu sang bàn mới?", () => {
    if (switchModalInstance) switchModalInstance.hide();

    fetch(
      `/api/tables/${selectedTableId}/switch?targetTableId=${targetTableId}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      },
    )
      .then(async (response) => {
        const data = await response.json();
        if (response.ok) {
          showSuccess("Đổi bàn thành công!");
          sessionStorage.setItem("pos_selected_table", targetTableId);
          setTimeout(() => window.location.reload(), 1500);
        } else {
          showError("Thất bại: " + data.message);
        }
      })
      .catch((e) => {
        console.error(e);
        showError("Lỗi kết nối máy chủ!");
      });
  });
}

// 9. TÍNH NĂNG HỦY KHÁCH (DỌN SẠCH BÀN)
function cancelTable() {
  if (!selectedTableId) return;

  const table = tableList.find((t) => t.id === selectedTableId);
  if (!table || table.status === "available") return;

  showConfirm(
    "Hủy khách",
    `Bạn có chắc chắn muốn HỦY KHÁCH ở Bàn ${table.tableNumber} không?\nToàn bộ món nước và ghi chú sẽ bị xóa sạch, bàn sẽ trở về trạng thái Trống!`,
    () => {
      fetch(`/api/tables/${selectedTableId}/checkout?status=cancelled`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      })
        .then(async (response) => {
          if (response.ok) {
            showSuccess("Đã hủy khách và dọn dẹp bàn thành công!");
            sessionStorage.removeItem("pos_selected_table");
            setTimeout(() => window.location.reload(), 1500);
          } else {
            const data = await response.json();
            showError("Thất bại: " + data.message);
          }
        })
        .catch((e) => {
          console.error(e);
          showError("Lỗi kết nối máy chủ!");
        });
    },
  );
}

// 10. TÍNH NĂNG GỘP BÀN (MERGE TABLE)
let mergeModalInstance = null;

function openMergeTableModal() {
  if (!selectedTableId) return;

  const currentTable = tableList.find((t) => t.id === selectedTableId);
  if (!currentTable || currentTable.status !== "available") {
    showError("Chỉ có bàn trống mới được thao tác Gộp bàn!");
    return;
  }

  const selectEl = document.getElementById("mergeTargetTableSelect");
  selectEl.innerHTML = "";

  const validTables = tableList
    .filter((t) => t.status !== "available" && t.status !== "merged")
    .sort((a, b) => a.tableNumber - b.tableNumber);

  if (validTables.length === 0) {
    selectEl.innerHTML = `<option value="">-- Không có bàn nào đang hoạt động --</option>`;
    selectEl.disabled = true;
  } else {
    selectEl.disabled = false;
    validTables.forEach((t) => {
      const option = document.createElement("option");
      option.value = t.id;
      option.textContent = `Bàn số ${t.tableNumber} (${getStatusText(t.status)})`;
      selectEl.appendChild(option);
    });
  }

  const modalEl = document.getElementById("mergeTableModal");
  mergeModalInstance = new bootstrap.Modal(modalEl);
  mergeModalInstance.show();
}

function confirmMergeTable() {
  const targetTableId = document.getElementById("mergeTargetTableSelect").value;
  if (!targetTableId) {
    showError("Vui lòng chọn một bàn hợp lệ để gộp!");
    return;
  }

  showConfirm("Gộp bàn", "Xác nhận gộp bàn này?", () => {
    if (mergeModalInstance) mergeModalInstance.hide();

    fetch(
      `/api/tables/${selectedTableId}/merge?targetTableId=${targetTableId}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      },
    )
      .then(async (response) => {
        const data = await response.json();
        if (response.ok) {
          showSuccess("Gộp bàn thành công!");
          setTimeout(() => window.location.reload(), 1500);
        } else {
          showError("Thất bại: " + data.message);
        }
      })
      .catch((e) => {
        console.error(e);
        showError("Lỗi kết nối máy chủ!");
      });
  });
}
