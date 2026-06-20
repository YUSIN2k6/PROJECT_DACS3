let selectedInvoice = null;
let currentModalPage = 1;
const MODAL_ITEMS_PER_PAGE = 10;

document.addEventListener("DOMContentLoaded", function () {
  attachEventListeners();
});

// 1. TÌM KIẾM TRÊN GIAO DIỆN HIỆN TẠI
function filterInvoices() {
  const searchInput = document.getElementById("searchInput");
  if (!searchInput) return;

  const filterText = searchInput.value.toLowerCase().trim();
  const rows = document.querySelectorAll(
    "#invoiceTableBody tr:not(#noDataRow)",
  );

  rows.forEach((row) => {
    const idCol = row.cells[0]?.textContent.toLowerCase() || "";
    const tableCol = row.cells[2]?.textContent.toLowerCase() || "";

    if (idCol.includes(filterText) || tableCol.includes(filterText)) {
      row.style.display = "";
    } else {
      row.style.display = "none";
    }
  });
}

// 2. XEM CHI TIẾT HOÁ ĐƠN (Dùng mảng currentInvoices do Java đẩy xuống)
function viewInvoiceDetails(invoiceId) {
  // Tìm hóa đơn trong danh sách trang hiện tại
  selectedInvoice = currentInvoices.find((inv) => inv.id === invoiceId);
  if (!selectedInvoice) return;

  const detailContent = document.getElementById("invoiceDetailContent");
  const emptyMessage = document.getElementById("invoiceEmptyMessage");

  const itemsArray = selectedInvoice.items || [];
  const totalItemsQty = itemsArray.reduce(
    (sum, item) => sum + item.quantity,
    0,
  );

  // Gắn thông tin khớp với biến Java Entity (invoiceDate, tableNumber, totalAmount)
  document.getElementById("invoiceId").textContent = selectedInvoice.id;
  document.getElementById("invoiceDate").textContent =
    selectedInvoice.invoiceDate;
  document.getElementById("invoiceTable").textContent =
    `Bàn ${selectedInvoice.tableNumber}`;
  document.getElementById("invoiceQty").textContent = totalItemsQty;
  document.getElementById("invoiceTotal").textContent =
    selectedInvoice.totalAmount.toLocaleString("vi-VN") + " đ";

  const statusText =
    selectedInvoice.status === "paid" ? "Đã thanh toán" : "Đã huỷ";
  const statusBadge = `<span class="status-badge ${selectedInvoice.status === "paid" ? "status-paid" : "status-cancelled"}">${statusText}</span>`;
  document.getElementById("invoiceStatus").innerHTML = statusBadge;

  // Highlight dòng vừa bấm
  document
    .querySelectorAll("#invoiceTableBody tr")
    .forEach((row) => row.classList.remove("active"));
  const selectedRow = document.querySelector(
    `tr[data-invoice-id="${selectedInvoice.id}"]`,
  );
  if (selectedRow) selectedRow.classList.add("active");

  detailContent.style.display = "block";
  emptyMessage.style.display = "none";
}

// MỞ MODAL XEM CÁC MÓN TRONG HÓA ĐƠN
function showInvoiceItemsModal() {
  if (!selectedInvoice) return;
  const itemsBody = document.getElementById("invoiceItemsBody");
  itemsBody.innerHTML = "";

  const itemsArray = selectedInvoice.items || [];

  itemsArray.forEach((item, index) => {
    // Biến Java là unitPrice, itemName
    const totalPrice = item.unitPrice * item.quantity;
    const row = document.createElement("tr");
    row.innerHTML = `
      <td style="text-align: center">${index + 1}</td>
      <td>${item.itemName}</td>
      <td style="text-align: center">${item.quantity}</td>
      <td style="text-align: right">${item.unitPrice.toLocaleString("vi-VN")} đ</td>
      <td style="text-align: right; font-weight: 600">${totalPrice.toLocaleString("vi-VN")} đ</td>
    `;
    itemsBody.appendChild(row);
  });
}

// 3. XUẤT EXCEL (Bản Nâng Cấp: Truy tìm bộ lọc thông minh)
function exportToExcel() {
  if (!allInvoicesData || allInvoicesData.length === 0) {
    showError("Không có dữ liệu hoá đơn để xuất!");
    return;
  }

  const searchInput = document.getElementById("searchInput");
  const filterText = searchInput ? searchInput.value.toLowerCase().trim() : "";

  // 1. TÌM KIẾM THÔNG MINH Ô CHỌN THỜI GIAN
  let timeFilterValue = "all";
  const timeSelect = document.getElementById("timeFilter");

  if (timeSelect) {
    timeFilterValue = timeSelect.value;
  } else {
    // Nếu không có id="timeFilter", tự động quét tìm thẻ select nào có chứa value "today"
    const allSelects = document.querySelectorAll("select");
    allSelects.forEach((select) => {
      const hasToday = Array.from(select.options).some(
        (opt) => opt.value === "today" || opt.value === "today_val",
      );
      if (hasToday) timeFilterValue = select.value;
    });
  }

  // TIẾN HÀNH LỌC DỮ LIỆU
  const filteredData = allInvoicesData.filter((invoice) => {
    if (!invoice || !invoice.id || !invoice.invoiceDate) return false;

    // --- Khớp ô tìm kiếm ---
    const idCol = String(invoice.id).toLowerCase();
    const tableCol = `bàn ${invoice.tableNumber}`.toLowerCase();
    const matchSearch =
      idCol.includes(filterText) || tableCol.includes(filterText);

    // --- Khớp thời gian ---
    let matchTime = true;

    // Đảm bảo value lấy được đúng chuẩn để so sánh
    const activeFilter = timeFilterValue.toLowerCase();

    if (activeFilter !== "all" && activeFilter !== "") {
      let invDate = new Date(invoice.invoiceDate.replace(" ", "T"));

      if (isNaN(invDate.getTime())) {
        const parts = invoice.invoiceDate.split(/[ \/:-]/);
        if (parts.length >= 3 && parts[2].length === 4) {
          invDate = new Date(
            `${parts[2]}-${parts[1]}-${parts[0]}T${parts[3] || "00"}:${parts[4] || "00"}:${parts[5] || "00"}`,
          );
        }
      }

      if (isNaN(invDate.getTime())) return false;

      const today = new Date();

      if (activeFilter.includes("today")) {
        matchTime = invDate.toDateString() === today.toDateString();
      } else if (activeFilter.includes("week")) {
        const firstDayOfWeek = new Date(today.getTime());
        const day = firstDayOfWeek.getDay();
        const diff = firstDayOfWeek.getDate() - day + (day === 0 ? -6 : 1);
        firstDayOfWeek.setDate(diff);
        firstDayOfWeek.setHours(0, 0, 0, 0);
        matchTime = invDate >= firstDayOfWeek;
      } else if (activeFilter.includes("month")) {
        matchTime =
          invDate.getMonth() === today.getMonth() &&
          invDate.getFullYear() === today.getFullYear();
      }
    }

    return matchSearch && matchTime;
  });

  if (filteredData.length === 0) {
    showError("Không có hoá đơn nào trong khoảng thời gian này để xuất Excel!");
    return;
  }

  // TẠO FILE EXCEL TỪ DANH SÁCH ĐÃ LỌC
  const data = [
    ["DANH SÁCH HÓA ĐƠN"],
    [],
    [
      "STT",
      "Mã hoá đơn",
      "Bàn",
      "Tổng hoá đơn (đ)",
      "Tổng SL",
      "Trạng thái",
      "Thời gian",
    ],
  ];

  filteredData.forEach((invoice, idx) => {
    const itemsArray = invoice.items || [];
    const totalQty = itemsArray.reduce((sum, item) => sum + item.quantity, 0);
    const statusText = invoice.status === "paid" ? "Đã thanh toán" : "Đã huỷ";

    data.push([
      idx + 1,
      invoice.id,
      `Bàn ${invoice.tableNumber}`,
      invoice.totalAmount,
      totalQty,
      statusText,
      invoice.invoiceDate,
    ]);
  });

  const ws = XLSX.utils.aoa_to_sheet(data);
  ws["!cols"] = [
    { wch: 8 },
    { wch: 18 },
    { wch: 10 },
    { wch: 18 },
    { wch: 12 },
    { wch: 18 },
    { wch: 20 },
  ];
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "Hoá Đơn");

  const now = new Date();
  const dateStr =
    now.getFullYear() +
    String(now.getMonth() + 1).padStart(2, "0") +
    String(now.getDate()).padStart(2, "0");
  XLSX.writeFile(wb, `DanhSachHoaDon_${dateStr}.xlsx`);

  showSuccess(`Đã xuất thành công ${filteredData.length} hóa đơn ra Excel!`);
}
function attachEventListeners() {
  const searchInput = document.getElementById("searchInput");
  if (searchInput) {
    searchInput.addEventListener("keyup", filterInvoices);
  }

  const exportBtn = document.getElementById("exportExcelBtn");
  if (exportBtn) {
    exportBtn.addEventListener("click", exportToExcel);
  }

  const modal = document.getElementById("invoiceDetailsModal");
  if (modal) {
    modal.addEventListener("show.bs.modal", showInvoiceItemsModal);
  }
}
