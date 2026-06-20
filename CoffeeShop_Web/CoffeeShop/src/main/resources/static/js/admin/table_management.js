/**
 * Admin Table Management - Giao diện hiển thị lưới bàn từ Firebase
 */

let tables = [];

document.addEventListener("DOMContentLoaded", function () {
  console.log("=== Table Management Page Initialized ===");
  loadTablesFromServer();
  renderTables();
});

/**
 * Đọc dữ liệu thật từ các thẻ span do Thymeleaf kết xuất
 */
function loadTablesFromServer() {
  const tablesDataDiv = document.getElementById("tablesData");
  if (tablesDataDiv) {
    const spans = tablesDataDiv.querySelectorAll("span");
    if (spans.length > 0) {
      tables = [];
      spans.forEach((span) => {
        const id = span.getAttribute("data-table-id");
        const number = parseInt(span.textContent.trim());

        if (id && !isNaN(number) && number > 0) {
          tables.push({
            id: id,
            number: number,
            status: "empty",
          });
        }
      });
    }
  }
}

/**
 * Kết xuất cấu trúc lưới bàn hiển thị lên màn hình (2 bàn mỗi cột)
 */
function renderTables() {
  const tablesGrid = document.getElementById("tablesGrid");
  if (!tablesGrid) return;
  tablesGrid.innerHTML = "";

  // Sắp xếp thứ tự tăng dần theo số bàn
  const sortedTables = [...tables].sort((a, b) => a.number - b.number);

  for (let i = 0; i < sortedTables.length; i += 2) {
    const columnEl = document.createElement("div");
    columnEl.className = "table-column";

    // Bàn thứ nhất trong cột
    const table1 = sortedTables[i];
    columnEl.appendChild(createTableElement(table1));

    // Bàn thứ hai trong cột nếu tồn tại
    if (i + 1 < sortedTables.length) {
      const table2 = sortedTables[i + 1];
      columnEl.appendChild(createTableElement(table2));
    }

    tablesGrid.appendChild(columnEl);
  }
}

/**
 * Tạo thành phần HTML cho từng bàn
 */
function createTableElement(table) {
  const tableEl = document.createElement("div");
  tableEl.className = "table-item";
  tableEl.innerHTML = `
    <div class="table-label">Bàn</div>
    <div class="table-number">${table.number}</div>
  `;

  tableEl.addEventListener("click", () => selectTable(table.id));
  return tableEl;
}

/**
 * Hiệu ứng kích hoạt chọn bàn khi Click
 */
function selectTable(tableId) {
  document.querySelectorAll(".table-item").forEach((item) => {
    item.classList.remove("active");
  });

  const selectedTableEls = document.querySelectorAll(".table-item");
  const sortedTables = [...tables].sort((a, b) => a.number - b.number);
  const selectedTable = sortedTables.find((t) => t.id === tableId);

  if (selectedTable) {
    const selectedIndex = sortedTables.indexOf(selectedTable);
    if (selectedTableEls[selectedIndex]) {
      selectedTableEls[selectedIndex].classList.add("active");
    }
  }
}
