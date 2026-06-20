// ===== NAVBAR USER DROPDOWN FUNCTIONALITY =====
console.log("navbar.js file loaded");

document.addEventListener("DOMContentLoaded", function () {
  const userProfileBtn = document.getElementById("userProfileBtn");
  const userDropdownMenu = document.getElementById("userDropdownMenu");

  console.log("Navbar DOMContentLoaded");
  console.log("User Profile Btn:", userProfileBtn);
  console.log("User Dropdown Menu:", userDropdownMenu);

  // Function to position dropdown
  function positionDropdown() {
    if (!userProfileBtn || !userDropdownMenu) return;

    const rect = userProfileBtn.getBoundingClientRect();
    userDropdownMenu.style.top = rect.bottom + 10 + "px";
    userDropdownMenu.style.right = window.innerWidth - rect.right + "px";
  }

  // Toggle dropdown menu khi click vào user avatar
  if (userProfileBtn && userDropdownMenu) {
    console.log("Setting up user profile button click handler");
    userProfileBtn.addEventListener("click", function (e) {
      console.log("User profile button clicked!");
      e.stopPropagation();
      positionDropdown();
      userDropdownMenu.classList.toggle("active");
      console.log("Dropdown menu classes:", userDropdownMenu.className);
    });
  }

  // Đóng dropdown khi click vào menu item
  const dropdownItems = document.querySelectorAll(".dropdown-item");
  dropdownItems.forEach((item) => {
    item.addEventListener("click", function (e) {
      // Nếu không phải link logout, đóng dropdown
      if (!item.classList.contains("logout-item")) {
        e.preventDefault();
        userDropdownMenu?.classList.remove("active");
      }
    });
  });

  // Đóng dropdown khi click outside
  document.addEventListener("click", function (e) {
    if (
      userDropdownMenu &&
      !userProfileBtn?.contains(e.target) &&
      !userDropdownMenu.contains(e.target)
    ) {
      userDropdownMenu.classList.remove("active");
    }
  });

  // Đóng dropdown khi press ESC
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && userDropdownMenu) {
      userDropdownMenu.classList.remove("active");
    }
  });
});
