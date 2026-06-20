// ===== SIDEBAR TOGGLE FUNCTIONALITY =====
console.log("sidebar.js file loaded");

document.addEventListener("DOMContentLoaded", function () {
  const sidebar = document.getElementById("sidebar");
  const toggleBtn = document.getElementById("toggleSidebar");
  const navToggleBtn = document.getElementById("navToggleSidebar");
  const submenus = document.querySelectorAll(".has-submenu");

  console.log("Sidebar JS loaded");
  console.log("Sidebar:", sidebar);
  console.log("Toggle Btn:", toggleBtn);
  console.log("Nav Toggle Btn:", navToggleBtn);

  if (navToggleBtn) {
    console.log("Adding click listener to navToggleBtn");
  }

  // Function để toggle sidebar
  function toggleSidebar() {
    const isDesktop = window.innerWidth >= 992;

    if (isDesktop) {
      // Desktop: toggle "collapsed" class
      sidebar.classList.toggle("collapsed");
      document.body.classList.toggle("sidebar-collapsed");
    } else {
      // Mobile/Tablet: toggle "active" class
      sidebar.classList.toggle("active");
    }

    // Lưu trạng thái vào localStorage (chỉ desktop)
    if (isDesktop) {
      localStorage.setItem(
        "sidebarCollapsed",
        sidebar.classList.contains("collapsed"),
      );
    }
  }

  // Sidebar toggle from navbar button
  if (navToggleBtn) {
    navToggleBtn.addEventListener("click", function(e) {
      console.log("Menu button clicked!");
      toggleSidebar();
    });
  }

  // Restore sidebar state từ localStorage (desktop only)
  const isCollapsed = localStorage.getItem("sidebarCollapsed") === "true";
  if (isCollapsed && window.innerWidth >= 992) {
    sidebar.classList.add("collapsed");
    document.body.classList.add("sidebar-collapsed");
  }

  // Submenu toggle
  submenus.forEach((submenu) => {
    const submenuToggle = submenu.querySelector(".submenu-toggle");
    if (submenuToggle) {
      submenuToggle.addEventListener("click", function (e) {
        e.preventDefault();
        submenu.classList.toggle("open");
      });
    }
  });

  // Đóng sidebar khi click vào menu item trên mobile/tablet
  const navLinks = document.querySelectorAll(".nav-link");
  navLinks.forEach((link) => {
    link.addEventListener("click", function () {
      if (window.innerWidth < 992) {
        sidebar.classList.remove("active");
      }
    });
  });

  // Đóng sidebar khi click outside trên mobile/tablet
  document.addEventListener("click", function (e) {
    if (window.innerWidth < 992) {
      const isSidebarClick = sidebar.contains(e.target);
      const isToggleBtnClick = navToggleBtn?.contains(e.target);
      const isToggleBtnClick2 = toggleBtn?.contains(e.target);

      if (!isSidebarClick && !isToggleBtnClick && !isToggleBtnClick2) {
        sidebar.classList.remove("active");
      }
    }
  });

  // Xử lý resize window
  window.addEventListener("resize", function () {
    const isDesktop = window.innerWidth >= 992;

    if (isDesktop) {
      // Remove "active" class when resize to desktop
      sidebar.classList.remove("active");
      // Restore collapsed state từ localStorage
      const isCollapsed = localStorage.getItem("sidebarCollapsed") === "true";
      if (isCollapsed) {
        sidebar.classList.add("collapsed");
        document.body.classList.add("sidebar-collapsed");
      } else {
        sidebar.classList.remove("collapsed");
        document.body.classList.remove("sidebar-collapsed");
      }
    } else {
      // Reset collapsed state when resize to mobile/tablet
      sidebar.classList.remove("collapsed");
      document.body.classList.remove("sidebar-collapsed");
    }
  });
});
