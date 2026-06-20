document.addEventListener("DOMContentLoaded", function () {
  // Cấu hình font chữ mặc định cho tất cả biểu đồ
  Chart.defaults.font.family =
    "'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif";
  Chart.defaults.color = "#6c757d";

  // Khởi tạo khung biểu đồ rỗng
  initRevenueChart();
  initTopDrinksChart();
  initCategoryChart();

  // Bơm dữ liệu thật lần đầu (mặc định lấy theo Tuần)
  fetchAndLoadDashboardData("week");

  // Xử lý sự kiện khi thay đổi bộ lọc "Tuần/Tháng/Năm"
  document
    .getElementById("revenueFilter")
    .addEventListener("change", function (e) {
      fetchAndLoadDashboardData(e.target.value);
    });
});

let revenueChartInstance = null;
let topDrinksChartInstance = null;
let categoryChartInstance = null;

// ==========================================
// HÀM CALL API LẤY DATA THẬT
// ==========================================
async function fetchAndLoadDashboardData(filterType) {
  try {
    const response = await fetch(`/admin/api/dashboard?filter=${filterType}`);
    if (!response.ok) throw new Error("Lỗi mạng khi tải dữ liệu");

    const data = await response.json();

    // 1. Cập nhật 4 ô thông số
    document.getElementById("statRevenue").innerText =
      data.monthRevenue.toLocaleString("vi-VN") + " đ";
    document.getElementById("statDrinks").innerText =
      data.totalMenuItems + " món";
    document.getElementById("statStaff").innerText = data.totalStaff + " người";
    document.getElementById("statTables").innerText = data.totalTables + " bàn";

    // 2. Cập nhật Biểu đồ Doanh thu (Col-12)
    revenueChartInstance.data.labels = data.revLabels;
    revenueChartInstance.data.datasets[0].data = data.revData;
    revenueChartInstance.update();

    // 3. Cập nhật Biểu đồ Top 5 Nước
    topDrinksChartInstance.data.labels = data.topDrinkLabels;
    topDrinksChartInstance.data.datasets[0].data = data.topDrinkData;
    topDrinksChartInstance.update();

    // 4. Cập nhật Biểu đồ Danh mục
    categoryChartInstance.data.labels = data.catLabels;
    categoryChartInstance.data.datasets[0].data = data.catData;
    categoryChartInstance.update();
  } catch (error) {
    console.error("Lỗi Fetch Data Dashboard:", error);
    // Nếu có thư viện custom-alert, có thể gọi showError("Lỗi tải dữ liệu!");
  }
}

// ==========================================
// CÁC HÀM VẼ KHUNG BIỂU ĐỒ (CHỜ BƠM DATA)
// ==========================================

// 1. BIỂU ĐỒ DOANH THU (CỘT DỌC)
function initRevenueChart() {
  const ctx = document.getElementById("revenueChart").getContext("2d");
  revenueChartInstance = new Chart(ctx, {
    type: "bar",
    data: {
      labels: [],
      datasets: [
        {
          label: "Doanh thu (VNĐ)",
          data: [],
          backgroundColor: "#1cc88a",
          borderRadius: 6,
          barPercentage: 0.5,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: function (context) {
              return " " + context.raw.toLocaleString("vi-VN") + " đ";
            },
          },
        },
      },
      scales: {
        y: { beginAtZero: true, grid: { borderDash: [5, 5] } },
        x: { grid: { display: false } },
      },
    },
  });
}

// 2. BIỂU ĐỒ TOP 5 NƯỚC (CỘT NGANG)
function initTopDrinksChart() {
  const ctx = document.getElementById("topDrinksChart").getContext("2d");
  topDrinksChartInstance = new Chart(ctx, {
    type: "bar",
    data: {
      labels: [],
      datasets: [
        {
          label: "Số lượng ly",
          data: [],
          backgroundColor: [
            "#1cc88a",
            "#36b9cc",
            "#f6c23e",
            "#e74a3b",
            "#858796",
          ],
          borderRadius: 6,
          barPercentage: 0.7,
        },
      ],
    },
    options: {
      indexAxis: "y",
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
      },
      scales: {
        x: { beginAtZero: true, grid: { borderDash: [5, 5] } },
        y: { grid: { display: false }, ticks: { font: { weight: "bold" } } },
      },
    },
  });
}

// 3. BIỂU ĐỒ DANH MỤC (TRÒN)
function initCategoryChart() {
  const ctx = document.getElementById("categoryChart").getContext("2d");
  categoryChartInstance = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: [],
      datasets: [
        {
          data: [],
          backgroundColor: [
            "#4e73df",
            "#1cc88a",
            "#36b9cc",
            "#f6c23e",
            "#e74a3b",
            "#f8f9fa",
          ],
          hoverOffset: 6,
          borderWidth: 2,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: "60%",
      plugins: {
        legend: {
          position: "bottom",
          labels: { padding: 20, usePointStyle: true },
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              return (
                " " +
                context.label +
                ": " +
                context.raw.toLocaleString("vi-VN") +
                " đ"
              );
            },
          },
        },
      },
    },
  });
}
