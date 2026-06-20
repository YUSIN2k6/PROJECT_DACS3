/**
 * Cashier Checkout - Xử lý dữ liệu thật từ Firebase Realtime Database
 */

let selectedTableNum = null;

document.addEventListener("DOMContentLoaded", function () {
  loadPendingTables();
  attachEventListeners();

  // TỰ ĐỘNG LẤY SỐ BÀN TỪ ĐƯỜNG DẪN URL (Ví dụ: ?table=1)
  const urlParams = new URLSearchParams(window.location.search);
  const tableParam = urlParams.get("table");
  if (tableParam) {
    const tableSelect = document.getElementById("tableSelect");
    if (tableSelect) {
      tableSelect.value = tableParam;
      // Tự động kích hoạt hiển thị dữ liệu bàn đó
      loadCheckoutData(parseInt(tableParam));
    }
  }
});

function loadPendingTables() {
  const selectEl = document.getElementById("tableSelect");
  selectEl.innerHTML =
    '<option value="">-- Chọn bàn cần thanh toán --</option>';

  pendingTables.forEach((table) => {
    const option = document.createElement("option");
    option.value = table.tableNumber;
    option.textContent = `Bàn ${table.tableNumber}`;
    selectEl.appendChild(option);
  });
}

function attachEventListeners() {
  document
    .getElementById("tableSelect")
    .addEventListener("change", function () {
      if (this.value) {
        loadCheckoutData(parseInt(this.value));
      } else {
        clearCheckoutDisplay();
      }
    });

  document
    .getElementById("refreshBtn")
    .addEventListener("click", refreshPendingTables);
  document
    .getElementById("checkoutBtn")
    .addEventListener("click", processCheckout);
  document
    .getElementById("printCheckoutBtn")
    .addEventListener("click", printCheckoutInvoice);
}

function loadCheckoutData(tableNum) {
  const tableData = pendingTables.find((t) => t.tableNumber === tableNum);
  if (!tableData) return;

  selectedTableNum = tableNum;

  const itemsBody = document.getElementById("checkoutItemsBody");
  itemsBody.innerHTML = "";

  const currentOrder = tableData.currentOrder;
  const items = currentOrder && currentOrder.items ? currentOrder.items : [];

  if (items.length === 0) {
    itemsBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Hoá đơn chưa có món nào hoặc lỗi cấu trúc đơn!</td></tr>`;
    document.getElementById("checkoutQty").textContent = "0";
    document.getElementById("checkoutTotal").textContent = "0 đ";
    return;
  }

  items.forEach((item, index) => {
    const nameOfProduct = item.item_name || item.itemName;
    const idOfProduct = item.item_id || item.itemId;

    const totalPrice = item.price * item.quantity;
    const row = document.createElement("tr");

    row.innerHTML = `
      <td style="text-align: center">${index + 1}</td>
      <td class="fw-bold">${nameOfProduct}</td> 
      <td style="text-align: center">
        <div class="d-flex justify-content-center align-items-center gap-1">
          <button class="btn btn-sm btn-outline-secondary px-2 py-0" onclick="changeCheckoutQty('${idOfProduct}', -1)">-</button>
          <span class="fw-bold px-2" style="min-width: 30px; display: inline-block;">${item.quantity}</span>
          <button class="btn btn-sm btn-outline-secondary px-2 py-0" onclick="changeCheckoutQty('${idOfProduct}', 1)">+</button>
        </div>
      </td>
      <td style="text-align: right">${item.price.toLocaleString("vi-VN")} đ</td>
      <td style="text-align: right; font-weight: 600; color: #d4a574">${totalPrice.toLocaleString("vi-VN")} đ</td>
      <td style="text-align: center">
        <button class="btn btn-sm btn-outline-danger border-0" onclick="deleteCheckoutItem('${idOfProduct}')" title="Xóa món">
          <i class="bx bx-trash fs-5"></i>
        </button>
      </td>
    `;
    itemsBody.appendChild(row);
  });

  document.getElementById("checkoutTableContainer").style.display = "block";
  document.getElementById("checkoutEmptyMessage").style.display = "none";

  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const calculatedTotal = items.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0,
  );

  tableData.calculatedTotalAmount = calculatedTotal;

  document.getElementById("checkoutTableNum").textContent = `Bàn ${tableNum}`;
  document.getElementById("checkoutQty").textContent = totalItems;
  document.getElementById("checkoutTotal").textContent =
    calculatedTotal.toLocaleString("vi-VN") + " đ";
  document.getElementById("statusSelect").value = "paid";

  document.getElementById("checkoutActionContent").style.display = "block";
  document.getElementById("checkoutEmptyAction").style.display = "none";
}

function changeCheckoutQty(itemId, delta) {
  if (!selectedTableNum) return;
  const tableData = pendingTables.find(
    (t) => t.tableNumber === selectedTableNum,
  );
  if (!tableData || !tableData.currentOrder || !tableData.currentOrder.items)
    return;

  const item = tableData.currentOrder.items.find(
    (i) => i.item_id === itemId || i.itemId === itemId,
  );
  if (item) {
    item.quantity += delta;
    if (item.quantity <= 0) {
      deleteCheckoutItem(itemId);
      return;
    }
    loadCheckoutData(selectedTableNum);
  }
}

// Thay thế hàm xác nhận xóa món
function deleteCheckoutItem(itemId) {
  if (!selectedTableNum) return;
  const tableData = pendingTables.find(
    (t) => t.tableNumber === selectedTableNum,
  );
  if (!tableData || !tableData.currentOrder || !tableData.currentOrder.items)
    return;

  const item = tableData.currentOrder.items.find(
    (i) => i.item_id === itemId || i.itemId === itemId,
  );
  if (!item) return;

  const nameOfProduct = item.item_name || item.itemName;

  // Dùng Hộp thoại Confirm Tùy chỉnh bất đồng bộ
  showConfirm(
    "Xóa Món",
    `Bạn có chắc chắn muốn xóa món "${nameOfProduct}" khỏi hóa đơn?`,
    () => {
      tableData.currentOrder.items = tableData.currentOrder.items.filter(
        (i) => i.item_id !== itemId && i.itemId !== itemId,
      );
      loadCheckoutData(selectedTableNum);
      showSuccess(
        `Đã xóa "${nameOfProduct}". Nhớ bấm Thanh Toán để lưu thay đổi!`,
      );
    },
  );
}

function clearCheckoutDisplay() {
  selectedTableNum = null;
  document.getElementById("checkoutTableContainer").style.display = "none";
  document.getElementById("checkoutEmptyMessage").style.display = "block";
  document.getElementById("checkoutActionContent").style.display = "none";
  document.getElementById("checkoutEmptyAction").style.display = "block";
}

function refreshPendingTables() {
  window.location.reload();
}

// Thay thế luồng xác nhận và thông báo thanh toán
function processCheckout() {
  if (!selectedTableNum) {
    showError("Vui lòng chọn bàn cần thanh toán!");
    return;
  }

  const status = document.getElementById("statusSelect").value;
  const tableData = pendingTables.find(
    (t) => t.tableNumber === selectedTableNum,
  );
  if (!tableData) return;

  const finalPrice = tableData.calculatedTotalAmount || 0;
  const actionText = status === "paid" ? "Thanh toán" : "Hủy hóa đơn";

  // Gọi Hộp thoại Confirm tuỳ chỉnh để hỏi thu ngân
  showConfirm(
    "Xác nhận thao tác",
    `Xác nhận ${actionText} cho Bàn ${selectedTableNum}?\nTổng số tiền: ${finalPrice.toLocaleString("vi-VN")} đ`,
    () => {
      // Nếu Thu ngân bấm [Đồng ý]
      const checkoutBtn = document.getElementById("checkoutBtn");
      const originalBtnHtml = checkoutBtn.innerHTML;
      checkoutBtn.innerHTML =
        "<i class='bx bx-loader-alt bx-spin'></i> Đang xử lý...";
      checkoutBtn.disabled = true;

      // API LƯU LỊCH SỬ VÀ DỌN BÀN
      fetch(`/api/tables/${tableData.id}/checkout?status=${status}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      })
        .then(async (response) => {
          if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText);
          }
          return response.json();
        })
        .then((data) => {
          showSuccess(`${actionText} thành công!\nHóa đơn đã được lưu.`);

          // Đợi 1.5 giây để người dùng đọc thông báo rồi đá thẳng về Dashboard
          setTimeout(() => {
            window.location.href = "/cashier/dashboardCashier";
          }, 1500);
        })
        .catch((err) => {
          console.error("Lỗi đồng bộ mạng: ", err);
          showError("Lỗi khi kết nối với máy chủ!");
        })
        .finally(() => {
          checkoutBtn.innerHTML = originalBtnHtml;
          checkoutBtn.disabled = false;
        });
    },
  ); // Kết thúc block của showConfirm
}

function printCheckoutInvoice() {
  if (!selectedTableNum) {
    showError("Vui lòng chọn bàn để in hóa đơn!");
    return;
  }

  const tableData = pendingTables.find(
    (t) => t.tableNumber === selectedTableNum,
  );
  if (!tableData || !tableData.currentOrder || !tableData.currentOrder.items)
    return;

  const items = tableData.currentOrder.items;
  const printWindow = window.open("", "", "width=600,height=800");
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const finalPrice = tableData.calculatedTotalAmount || 0;

  let itemsHtml = items
    .map((item, idx) => {
      const nameOfProduct = item.item_name || item.itemName;
      const totalPrice = item.price * item.quantity;
      return `
        <tr>
          <td style="text-align: center">${idx + 1}</td>
          <td>${nameOfProduct}</td>
          <td style="text-align: center">${item.quantity}</td>
          <td style="text-align: right">${item.price.toLocaleString("vi-VN")} đ</td>
          <td style="text-align: right">${totalPrice.toLocaleString("vi-VN")} đ</td>
        </tr>
      `;
    })
    .join("");

  const content = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .invoice-header { text-align: center; margin-bottom: 20px; }
        .invoice-header h1 { margin: 0; font-size: 24px; }
        .invoice-info { margin-bottom: 20px; display: grid; grid-template-columns: 1fr 1fr; font-size: 14px; }
        .invoice-info div { margin: 5px 0; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; font-size: 14px; }
        th { background-color: #f0f0f0; padding: 10px; text-align: left; border: 1px solid #ddd; }
        td { padding: 10px; border: 1px solid #ddd; }
        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
      </style>
    </head>
    <body>
      <div class="invoice-header">
        <h1>HÓA ĐƠN TẠM TÍNH</h1>
        <h3 style="margin:5px 0; color:#8d6e63;">COFFEE SHOP</h3>
      </div>
      <div class="invoice-info">
        <div><strong>Bàn:</strong> Số ${selectedTableNum}</div>
        <div><strong>Ngày:</strong> ${new Date().toLocaleString("vi-VN")}</div>
        <div><strong>Tổng SL món:</strong> ${totalItems}</div>
      </div>
      <table>
        <thead>
          <tr>
            <th style="width: 40px; text-align:center;">STT</th>
            <th>Tên nước</th>
            <th style="width: 50px; text-align: center;">SL</th>
            <th style="width: 90px; text-align: right;">Giá</th>
            <th style="width: 100px; text-align: right;">Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          ${itemsHtml}
        </tbody>
      </table>
      <div style="text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px;">
        Tổng cộng: ${finalPrice.toLocaleString("vi-VN")} đ
      </div>
      <div class="footer">
        <p>Cảm ơn quý khách. Hẹn gặp lại!</p>
        <p>Hệ thống quản lý nội bộ POS v1.0</p>
      </div>
    </body>
    </html>
  `;

  printWindow.document.write(content);
  printWindow.document.close();

  // Thông báo in thành công góc màn hình
  showSuccess("Đang tiến hành in hóa đơn...");

  setTimeout(() => {
    printWindow.print();
  }, 500);
}
