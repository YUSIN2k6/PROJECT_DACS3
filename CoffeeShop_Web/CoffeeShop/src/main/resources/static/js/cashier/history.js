/**
 * Cashier History - Giao diện Lịch sử giao dịch (Đã kết nối Firebase)
 */

let currentPage = 1;
const ITEMS_PER_PAGE = 10;
let filteredList = [];
let selectedInvoice = null;
let currentModalPage = 1;
const MODAL_ITEMS_PER_PAGE = 10;

/**
 * Initialize history page
 */
document.addEventListener("DOMContentLoaded", function () {
  // Biến invoiceList đã được Thymeleaf nạp vào từ HTML
  filteredList = [...invoiceList];
  loadInvoiceTable();
  attachEventListeners();
});

/**
 * Load invoice table
 */
function loadInvoiceTable() {
  const tableBody = document.getElementById("invoiceTableBody");
  tableBody.innerHTML = "";

  if (filteredList.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Không tìm thấy hóa đơn nào</td></tr>`;
    updateInvoicePagination();
    return;
  }

  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const endIndex = startIndex + ITEMS_PER_PAGE;
  const paginatedList = filteredList.slice(startIndex, endIndex);

  paginatedList.forEach((invoice) => {
    const statusClass =
      invoice.status === "paid" ? "status-paid" : "status-cancelled";
    const statusText = invoice.status === "paid" ? "Đã thanh toán" : "Đã huỷ";

    // Đổi hiển thị Bàn 0 thành Mang về
    const tableText =
      invoice.tableNumber === 0 ? "Bàn 0" : `Bàn ${invoice.tableNumber}`;

    const row = document.createElement("tr");
    row.setAttribute("data-invoice-id", invoice.id);
    row.innerHTML = `
      <td><strong>${invoice.id}</strong></td>
      <td>${invoice.invoiceDate}</td>
      <td>${tableText}</td>
      <td class="fw-bold" style="color: #d4a574">${invoice.totalAmount.toLocaleString("vi-VN")} đ</td>
      <td><span class="status-badge ${statusClass}">${statusText}</span></td>
      <td style="text-align: center">
        <button class="btn btn-sm btn-primary" onclick="viewInvoiceDetails('${invoice.id}')">
          <i class="bx bxs-show"></i> Xem
        </button>
      </td>
    `;
    row.addEventListener("click", () => selectInvoice(invoice.id));
    tableBody.appendChild(row);
  });

  updateInvoicePagination();
}

/**
 * Phân trang
 */
function updateInvoicePagination() {
  const totalPages = Math.ceil(filteredList.length / ITEMS_PER_PAGE) || 1;
  const paginationContainer = document.getElementById(
    "invoicePaginationContainer",
  );
  if (!paginationContainer) return;
  paginationContainer.innerHTML = "";

  const prevLi = document.createElement("li");
  prevLi.className = `page-item ${currentPage === 1 ? "disabled" : ""}`;
  prevLi.innerHTML = `<a class="page-link" href="#" onclick="goToInvoicePage(${currentPage - 1})">Trước</a>`;
  paginationContainer.appendChild(prevLi);

  for (let i = 1; i <= totalPages; i++) {
    const li = document.createElement("li");
    li.className = `page-item ${i === currentPage ? "active" : ""}`;
    li.innerHTML = `<a class="page-link" href="#" onclick="goToInvoicePage(${i})">${i}</a>`;
    paginationContainer.appendChild(li);
  }

  const nextLi = document.createElement("li");
  nextLi.className = `page-item ${currentPage === totalPages ? "disabled" : ""}`;
  nextLi.innerHTML = `<a class="page-link" href="#" onclick="goToInvoicePage(${currentPage + 1})">Tiếp</a>`;
  paginationContainer.appendChild(nextLi);
}

function goToInvoicePage(page) {
  const totalPages = Math.ceil(filteredList.length / ITEMS_PER_PAGE);
  if (page >= 1 && page <= totalPages) {
    currentPage = page;
    loadInvoiceTable();
  }
}

/**
 * Xem chi tiết Hóa đơn
 */
function selectInvoice(invoiceId) {
  selectedInvoice = invoiceList.find((inv) => inv.id === invoiceId);
  if (selectedInvoice) {
    displayInvoiceDetails();
  }
}

function viewInvoiceDetails(invoiceId) {
  selectInvoice(invoiceId);
}

function displayInvoiceDetails() {
  if (!selectedInvoice) return;

  const detailContent = document.getElementById("invoiceDetailContent");
  const emptyMessage = document.getElementById("invoiceEmptyMessage");

  const itemsArray = selectedInvoice.items || [];
  const totalItems = itemsArray.reduce((sum, item) => sum + item.quantity, 0);

  const tableText =
    selectedInvoice.tableNumber === 0
      ? "Bàn 0"
      : `Bàn ${selectedInvoice.tableNumber}`;

  document.getElementById("invoiceId").textContent = selectedInvoice.id;
  document.getElementById("invoiceDate").textContent =
    selectedInvoice.invoiceDate;
  document.getElementById("invoiceTable").textContent = tableText;
  document.getElementById("invoiceQty").textContent = totalItems;
  document.getElementById("invoiceTotal").textContent =
    selectedInvoice.totalAmount.toLocaleString("vi-VN") + " đ";

  const statusText =
    selectedInvoice.status === "paid" ? "Đã thanh toán" : "Đã huỷ";
  const statusBadge = `<span class="status-badge ${selectedInvoice.status === "paid" ? "status-paid" : "status-cancelled"}">${statusText}</span>`;
  document.getElementById("invoiceStatus").innerHTML = statusBadge;

  document
    .querySelectorAll("#invoiceTableBody tr")
    .forEach((row) => row.classList.remove("active"));
  const selectedRow = document.querySelector(
    `tr[data-invoice-id="${selectedInvoice.id}"]`,
  );
  if (selectedRow) selectedRow.classList.add("active");

  detailContent.style.display = "block";
  emptyMessage.style.display = "none";
  currentModalPage = 1;
}

/**
 * Hiển thị danh sách món (Trong Modal)
 */
function showInvoiceItemsModal() {
  currentModalPage = 1;
  loadModalInvoiceItems();
}

function loadModalInvoiceItems() {
  if (!selectedInvoice) return;
  const itemsBody = document.getElementById("invoiceItemsBody");
  itemsBody.innerHTML = "";

  const itemsArray = selectedInvoice.items || [];
  const startIndex = (currentModalPage - 1) * MODAL_ITEMS_PER_PAGE;
  const endIndex = startIndex + MODAL_ITEMS_PER_PAGE;
  const paginatedItems = itemsArray.slice(startIndex, endIndex);

  paginatedItems.forEach((item, index) => {
    const rowNum = startIndex + index + 1;
    const totalPrice = item.unitPrice * item.quantity;

    const row = document.createElement("tr");
    row.innerHTML = `
      <td style="text-align: center">${rowNum}</td>
      <td>${item.itemName}</td>
      <td style="text-align: center">${item.quantity}</td>
      <td style="text-align: right">${item.unitPrice.toLocaleString("vi-VN")} đ</td>
      <td style="text-align: right; font-weight: 600">${totalPrice.toLocaleString("vi-VN")} đ</td>
    `;
    itemsBody.appendChild(row);
  });
  updateModalPagination();
}

function updateModalPagination() {
  if (!selectedInvoice) return;
  const itemsArray = selectedInvoice.items || [];
  const totalPages = Math.ceil(itemsArray.length / MODAL_ITEMS_PER_PAGE) || 1;
  const paginationContainer = document.getElementById(
    "modalPaginationContainer",
  );
  if (!paginationContainer) return;
  paginationContainer.innerHTML = "";

  const prevLi = document.createElement("li");
  prevLi.className = `page-item ${currentModalPage === 1 ? "disabled" : ""}`;
  prevLi.innerHTML = `<a class="page-link" href="#" onclick="goToModalPage(${currentModalPage - 1})">Trước</a>`;
  paginationContainer.appendChild(prevLi);

  for (let i = 1; i <= totalPages; i++) {
    const li = document.createElement("li");
    li.className = `page-item ${i === currentModalPage ? "active" : ""}`;
    li.innerHTML = `<a class="page-link" href="#" onclick="goToModalPage(${i})">${i}</a>`;
    paginationContainer.appendChild(li);
  }

  const nextLi = document.createElement("li");
  nextLi.className = `page-item ${currentModalPage === totalPages ? "disabled" : ""}`;
  nextLi.innerHTML = `<a class="page-link" href="#" onclick="goToModalPage(${currentModalPage + 1})">Tiếp</a>`;
  paginationContainer.appendChild(nextLi);
}

function goToModalPage(page) {
  if (!selectedInvoice) return;
  const itemsArray = selectedInvoice.items || [];
  const totalPages = Math.ceil(itemsArray.length / MODAL_ITEMS_PER_PAGE);
  if (page >= 1 && page <= totalPages) {
    currentModalPage = page;
    loadModalInvoiceItems();
  }
}

/**
 * In hóa đơn (Pop-up cửa sổ in)
 */
function printInvoice() {
  if (!selectedInvoice) return;
  const itemsArray = selectedInvoice.items || [];
  const printWindow = window.open("", "", "width=600,height=800");
  const totalItems = itemsArray.reduce((sum, item) => sum + item.quantity, 0);

  const tableText =
    selectedInvoice.tableNumber === 0
      ? "Bàn 0"
      : `Bàn ${selectedInvoice.tableNumber}`;

  let itemsHtml = itemsArray
    .map((item, idx) => {
      const totalPrice = item.unitPrice * item.quantity;
      return `
        <tr>
          <td style="text-align: center">${idx + 1}</td>
          <td>${item.itemName}</td>
          <td style="text-align: center">${item.quantity}</td>
          <td style="text-align: right">${item.unitPrice.toLocaleString("vi-VN")} đ</td>
          <td style="text-align: right">${totalPrice.toLocaleString("vi-VN")} đ</td>
        </tr>
      `;
    })
    .join("");

  const content = `
    <!DOCTYPE html>
    <html>
    <head>
      <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .invoice-header { text-align: center; margin-bottom: 20px; }
        .invoice-header h1 { margin: 0; }
        .invoice-info { margin-bottom: 20px; display: grid; grid-template-columns: 1fr 1fr; }
        .invoice-info div { margin: 5px 0; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th { background-color: #f0f0f0; padding: 10px; text-align: left; border: 1px solid #ddd; }
        td { padding: 10px; border: 1px solid #ddd; }
        .total-row { font-weight: bold; font-size: 18px; }
        .footer { text-align: center; margin-top: 30px; font-size: 12px; }
      </style>
    </head>
    <body>
      <div class="invoice-header">
        <h1>HÓA ĐƠN COFFEESHOP</h1>
      </div>
      <div class="invoice-info">
        <div><strong>Mã hoá đơn:</strong> ${selectedInvoice.id}</div>
        <div><strong>Ngày:</strong> ${selectedInvoice.invoiceDate}</div>
        <div><strong>Bàn:</strong> ${tableText}</div>
        <div><strong>Tổng SL:</strong> ${totalItems}</div>
      </div>
      <table>
        <thead>
          <tr>
            <th style="width: 50px;">STT</th>
            <th>Tên nước</th>
            <th style="width: 70px; text-align: center;">SL</th>
            <th style="width: 100px; text-align: right;">Giá</th>
            <th style="width: 100px; text-align: right;">Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          ${itemsHtml}
        </tbody>
      </table>
      <div style="text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px;">
        Tổng cộng: ${selectedInvoice.totalAmount.toLocaleString("vi-VN")} đ
      </div>
      <div class="footer">
        <p>Cảm ơn quý khách!</p>
        <p>In lúc: ${new Date().toLocaleString("vi-VN")}</p>
      </div>
    </body>
    </html>
  `;

  printWindow.document.write(content);
  printWindow.document.close();
  // Đợi hình ảnh/style load xong rồi mới bật hộp thoại In
  setTimeout(() => {
    printWindow.print();
  }, 500);
}

/**
 * Filter và tìm kiếm
 */
function filterInvoices() {
  const searchValue = document
    .getElementById("searchInput")
    .value.toLowerCase();
  const filterValue = document.getElementById("filterStatus").value;

  filteredList = invoiceList.filter((invoice) => {
    const matchSearch =
      invoice.id.toLowerCase().includes(searchValue) ||
      invoice.tableNumber.toString().includes(searchValue) ||
      invoice.totalAmount.toString().includes(searchValue);

    const matchFilter = filterValue === "all" || invoice.status === filterValue;

    return matchSearch && matchFilter;
  });

  currentPage = 1;
  loadInvoiceTable();
}

function attachEventListeners() {
  const searchInput = document.getElementById("searchInput");
  if (searchInput) {
    searchInput.addEventListener("keyup", filterInvoices);
  }

  const filterStatus = document.getElementById("filterStatus");
  if (filterStatus) {
    filterStatus.addEventListener("change", filterInvoices);
  }

  const printBtn = document.getElementById("printBtn");
  if (printBtn) {
    printBtn.addEventListener("click", printInvoice);
  }

  const modal = document.getElementById("invoiceDetailsModal");
  if (modal) {
    modal.addEventListener("show.bs.modal", showInvoiceItemsModal);
  }
}
