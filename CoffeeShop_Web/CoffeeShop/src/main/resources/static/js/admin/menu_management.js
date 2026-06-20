// ===== MENU MANAGEMENT PAGE FUNCTIONALITY =====

// Load page on document ready
document.addEventListener("DOMContentLoaded", function () {
  setupSearchAndFilter();
  setupActionButtons();
  setupCategoryForm();
  setupMenuImageUpload();
  setupMenuForm();
});

// Setup search and filter functionality (Frontend search on current page)
function setupSearchAndFilter() {
  const searchInput = document.getElementById("searchMenu");
  const tableBody = document.getElementById("menuTableBody");

  if (searchInput && tableBody) {
    searchInput.addEventListener("input", function () {
      const filterText = searchInput.value.toLowerCase().trim();
      const rows = tableBody.getElementsByTagName("tr");

      for (let i = 0; i < rows.length; i++) {
        if (rows[i].cells.length <= 1) continue;

        const nameCol = rows[i].cells[2]
          ? rows[i].cells[2].textContent.toLowerCase()
          : "";

        if (nameCol.includes(filterText)) {
          rows[i].style.display = "";
        } else {
          rows[i].style.display = "none";
        }
      }
    });
  }
}

// Setup action buttons for Edit and Delete
function setupActionButtons() {
  const menuTableBody = document.getElementById("menuTableBody");
  const categoryListContainer = document.getElementById(
    "categoryListContainer",
  );

  // Event Delegation for Menu Items
  if (menuTableBody) {
    menuTableBody.addEventListener("click", function (e) {
      const editBtn = e.target.closest(".edit-menu-btn");
      const deleteBtn = e.target.closest(".delete-menu-btn");

      if (editBtn) {
        const id = editBtn.getAttribute("data-id");
        editMenu(id);
      } else if (deleteBtn) {
        const id = deleteBtn.getAttribute("data-id");
        deleteMenu(id);
      }
    });
  }

  // Event Delegation for Categories
  if (categoryListContainer) {
    categoryListContainer.addEventListener("click", function (e) {
      const editBtn = e.target.closest(".edit-cat-btn");
      const deleteBtn = e.target.closest(".delete-cat-btn");

      if (editBtn) {
        const id = editBtn.getAttribute("data-id");
        editCategory(id);
      } else if (deleteBtn) {
        const id = deleteBtn.getAttribute("data-id");
        deleteCategory(id);
      }
    });
  }
}

function setupCategoryForm() {
  const categoryForm = document.getElementById("categoryForm");
  const categoryIdInput = document.getElementById("categoryId");
  const categoryNameInput = document.getElementById("categoryName");
  const categoryFormTitle = document.getElementById("categoryFormTitle");

  if (!categoryForm || !categoryNameInput) return;

  categoryForm.addEventListener("submit", async function (e) {
    e.preventDefault();

    const id = categoryIdInput ? categoryIdInput.value.trim() : "";
    const name = categoryNameInput.value;

    const isUpdate = !!id;
    const url = isUpdate
      ? "/admin/api/categories/" + encodeURIComponent(id)
      : "/admin/api/categories";
    const method = isUpdate ? "PUT" : "POST";

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
      });

      const data = await res.json().catch(() => null);
      if (!res.ok) {
        showError((data && data.message) || "Không thể lưu danh mục!");
        return;
      }

      showSuccess(
        isUpdate
          ? "Cập nhật danh mục thành công."
          : "Thêm danh mục thành công.",
      );

      // Đợi thông báo chạy 1.5s rồi tải lại trang
      setTimeout(() => window.location.reload(), 1500);
    } catch (err) {
      console.error(err);
      showError("Không thể kết nối server!");
    }
  });

  categoryForm.addEventListener("reset", function () {
    if (categoryIdInput) categoryIdInput.value = "";
    if (categoryFormTitle)
      categoryFormTitle.textContent = "Thêm/Cập nhật Danh Mục";
  });
}

function setupMenuImageUpload() {
  const menuImageInput = document.getElementById("menuImage");
  const menuImageUrlInput = document.getElementById("menuImageUrl");
  const menuImageHelp = document.getElementById("menuImageHelp");

  if (!menuImageInput || !menuImageUrlInput) return;

  menuImageInput.addEventListener("change", async function () {
    const file = menuImageInput.files && menuImageInput.files[0];
    if (!file) return;

    menuImageUrlInput.value = "";
    if (menuImageHelp) menuImageHelp.textContent = "Đang tải ảnh lên...";

    try {
      const formData = new FormData();
      formData.append("file", file);

      const res = await fetch("/admin/api/uploads/menu-image", {
        method: "POST",
        body: formData,
      });

      const data = await res.json().catch(() => null);
      if (!res.ok) {
        if (menuImageHelp)
          menuImageHelp.textContent = "Chọn file ảnh (JPG, PNG)";
        showError((data && data.message) || "Upload ảnh thất bại!");
        return;
      }

      menuImageUrlInput.value = data.imageUrl || "";
      if (menuImageHelp) {
        menuImageHelp.textContent = "Upload ảnh thành công.";
        menuImageHelp.style.color = "#198754"; // Chuyển chữ thành màu xanh báo thành công
      }
    } catch (err) {
      console.error(err);
      if (menuImageHelp) menuImageHelp.textContent = "Chọn file ảnh (JPG, PNG)";
      showError("Không thể kết nối server!");
    }
  });
}

function setupMenuForm() {
  const menuForm = document.getElementById("menuForm");
  const menuIdInput = document.getElementById("menuId");
  const menuNameInput = document.getElementById("menuName");
  const menuCategoryInput = document.getElementById("menuCategory");
  const menuPriceInput = document.getElementById("menuPrice");
  const menuImageUrlInput = document.getElementById("menuImageUrl");
  const menuImageHelp = document.getElementById("menuImageHelp");
  const formTitle = document.getElementById("formTitle");

  if (!menuForm || !menuNameInput || !menuCategoryInput || !menuPriceInput)
    return;

  menuForm.addEventListener("submit", async function (e) {
    e.preventDefault();

    const id = menuIdInput ? menuIdInput.value.trim() : "";

    const name = menuNameInput.value;
    const categoryId = menuCategoryInput.value;
    const price = menuPriceInput.value;
    const imageUrl = menuImageUrlInput ? menuImageUrlInput.value : "";

    const isUpdate = !!id;
    const url = isUpdate
      ? "/admin/api/menu-items/" + encodeURIComponent(id)
      : "/admin/api/menu-items";
    const method = isUpdate ? "PUT" : "POST";

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name,
          categoryId,
          price: Number(price),
          imageUrl,
        }),
      });

      const data = await res.json().catch(() => null);
      if (!res.ok) {
        showError(
          (data && data.message) ||
            (isUpdate ? "Cập nhật món thất bại!" : "Thêm món thất bại!"),
        );
        return;
      }

      showSuccess(
        isUpdate
          ? "Cập nhật món thành công."
          : "Thêm món thành công (mặc định: ngừng bán).",
      );

      setTimeout(() => window.location.reload(), 1500);
    } catch (err) {
      console.error(err);
      showError("Không thể kết nối server!");
    }
  });

  menuForm.addEventListener("reset", function () {
    if (menuIdInput) menuIdInput.value = "";
    if (menuImageUrlInput) menuImageUrlInput.value = "";
    if (menuImageHelp) {
      menuImageHelp.textContent = "Chọn file ảnh (JPG, PNG)";
      menuImageHelp.style.color = "";
    }
    if (formTitle) formTitle.textContent = "Thêm/Cập nhật Món";
  });
}

// Edit menu
async function editMenu(id) {
  const menuIdInput = document.getElementById("menuId");
  const menuNameInput = document.getElementById("menuName");
  const menuCategoryInput = document.getElementById("menuCategory");
  const menuPriceInput = document.getElementById("menuPrice");
  const menuImageUrlInput = document.getElementById("menuImageUrl");
  const menuImageHelp = document.getElementById("menuImageHelp");
  const formTitle = document.getElementById("formTitle");

  const menuId = (id || "").trim();
  if (!menuId) return;

  try {
    const res = await fetch(
      "/admin/api/menu-items/" + encodeURIComponent(menuId),
      {
        method: "GET",
        headers: { Accept: "application/json" },
      },
    );

    const data = await res.json().catch(() => null);
    if (!res.ok) {
      showError((data && data.message) || "Không thể tải món!");
      return;
    }

    if (menuIdInput) menuIdInput.value = data.id || menuId;
    if (menuNameInput) menuNameInput.value = data.name || "";
    if (menuCategoryInput) menuCategoryInput.value = data.categoryId || "";
    if (menuPriceInput)
      menuPriceInput.value = data.price != null ? data.price : "";

    const imageUrl = data.imageUrl || "";
    if (menuImageUrlInput) menuImageUrlInput.value = imageUrl;
    if (menuImageHelp) {
      menuImageHelp.textContent = imageUrl
        ? "Đã có ảnh. Chọn ảnh mới nếu muốn thay."
        : "Chọn file ảnh (JPG, PNG)";
      menuImageHelp.style.color = imageUrl ? "#198754" : "";
    }

    if (formTitle) formTitle.textContent = "Cập nhật Món";

    if (menuNameInput) {
      menuNameInput.focus();
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  } catch (err) {
    console.error(err);
    showError("Không thể kết nối server!");
  }
}

// Delete menu
function deleteMenu(id) {
  const menuId = (id || "").trim();
  if (!menuId) return;

  // Sử dụng hàm xác nhận tuỳ chỉnh bất đồng bộ
  showConfirm(
    "Xóa Món Nước",
    "Bạn có chắc chắn muốn xóa món này khỏi hệ thống không?",
    () => {
      fetch("/admin/api/menu-items/" + encodeURIComponent(menuId), {
        method: "DELETE",
        headers: { Accept: "application/json" },
      })
        .then(async (res) => {
          const data = await res.json().catch(() => null);
          if (!res.ok) {
            showError((data && data.message) || "Xóa món thất bại!");
            return;
          }
          showSuccess("Xóa món thành công.");
          setTimeout(() => window.location.reload(), 1500);
        })
        .catch((err) => {
          console.error(err);
          showError("Không thể kết nối server!");
        });
    },
  );
}

// Edit category
async function editCategory(id) {
  const categoryIdInput = document.getElementById("categoryId");
  const categoryNameInput = document.getElementById("categoryName");
  const categoryFormTitle = document.getElementById("categoryFormTitle");

  const categoryId = (id || "").trim();
  if (!categoryId) return;

  try {
    const res = await fetch(
      "/admin/api/categories/" + encodeURIComponent(categoryId),
      {
        method: "GET",
        headers: { Accept: "application/json" },
      },
    );

    const data = await res.json().catch(() => null);
    if (!res.ok) {
      showError((data && data.message) || "Không thể tải danh mục!");
      return;
    }

    if (categoryIdInput) categoryIdInput.value = data.id || categoryId;
    if (categoryNameInput) categoryNameInput.value = data.name || "";
    if (categoryFormTitle) categoryFormTitle.textContent = "Cập nhật Danh Mục";

    if (categoryNameInput) {
      categoryNameInput.focus();
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  } catch (err) {
    console.error(err);
    showError("Không thể kết nối server!");
  }
}

// Delete category
function deleteCategory(id) {
  const categoryId = (id || "").trim();
  if (!categoryId) return;

  // Sử dụng hàm xác nhận tuỳ chỉnh bất đồng bộ
  showConfirm(
    "Xóa Danh Mục",
    "Bạn có chắc chắn muốn xóa danh mục này? Các món thuộc danh mục này cũng có thể bị ảnh hưởng.",
    () => {
      fetch("/admin/api/categories/" + encodeURIComponent(categoryId), {
        method: "DELETE",
        headers: { Accept: "application/json" },
      })
        .then(async (res) => {
          const data = await res.json().catch(() => null);
          if (!res.ok) {
            showError((data && data.message) || "Xóa danh mục thất bại!");
            return;
          }
          showSuccess("Xóa danh mục thành công.");
          setTimeout(() => window.location.reload(), 3000);
        })
        .catch((err) => {
          console.error(err);
          showError("Không thể kết nối server!");
        });
    },
  );
}
