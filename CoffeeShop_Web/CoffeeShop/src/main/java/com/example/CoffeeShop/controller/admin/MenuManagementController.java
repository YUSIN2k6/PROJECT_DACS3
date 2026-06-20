package com.example.CoffeeShop.controller.admin;

import com.example.CoffeeShop.entity.Category;
import com.example.CoffeeShop.entity.MenuItem;
import com.example.CoffeeShop.service.CategoryService;
import com.example.CoffeeShop.service.MenuItemService;
import com.example.CoffeeShop.util.PageSlice;
import com.example.CoffeeShop.util.PaginationUtils;
import com.example.CoffeeShop.web.ConflictException;
import com.example.CoffeeShop.web.InternalServerException;
import com.example.CoffeeShop.web.NotFoundException;
import com.example.CoffeeShop.web.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * MenuManagementController - Xử lý trang quản lý Menu
 */
@Controller
public class MenuManagementController {

    private final MenuItemService menuItemService;
    private final CategoryService categoryService;

    public MenuManagementController(MenuItemService menuItemService, CategoryService categoryService) {
        this.menuItemService = menuItemService;
        this.categoryService = categoryService;
    }

    /**
     * Hiển thị trang quản lý Menu
     */
    @GetMapping("/admin/menu_management")
    public String menuManagementPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        try {
            // 1. Lấy dữ liệu từ Firebase
            List<MenuItem> allItems = menuItemService.getAllMenuItems().get();
            List<Category> allCategories = categoryService.getAllCategories().get();

            if (allItems == null)
                allItems = new ArrayList<>();
            if (allCategories == null)
                allCategories = new ArrayList<>();

            PageSlice<MenuItem> slice = PaginationUtils.slice(allItems, page, size);
            slice.applyToModel(model, "menuList");
            model.addAttribute("categoryList", allCategories);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("menuList", new ArrayList<>());
            model.addAttribute("categoryList", new ArrayList<>());
            model.addAttribute("message", "Lỗi khi kết nối cơ sở dữ liệu Firebase!");
            model.addAttribute("messageType", "error");
        }
        return "admin/menu_management";
    }

    public record CategoryUpsertRequest(String name) {
    }

    @PostMapping("/admin/api/categories")
    @ResponseBody
    public ResponseEntity<?> createCategory(@RequestBody CategoryUpsertRequest request) {
        String name = request == null ? null : request.name();
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(name));
    }

    @GetMapping("/admin/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> getCategory(@PathVariable String id) {
        try {
            Category category = categoryService.getCategoryById(id).get();
            if (category == null) {
                throw new NotFoundException("Không tìm thấy danh mục!");
            }
            return ResponseEntity.ok(category);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Không thể tải danh mục!");
        }
    }

    @PutMapping("/admin/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCategory(@PathVariable String id, @RequestBody CategoryUpsertRequest request) {
        String name = request == null ? null : request.name();
        return ResponseEntity.ok(categoryService.updateCategoryName(id, name));
    }

    @DeleteMapping("/admin/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        try {
            List<MenuItem> allItems = menuItemService.getAllMenuItems().get();
            if (allItems != null) {
                for (MenuItem item : allItems) {
                    if (item == null)
                        continue;
                    if (item.getCategoryId() != null && item.getCategoryId().equals(id)) {
                        throw new ConflictException("Không thể xoá danh mục vì đang có món thuộc danh mục này!");
                    }
                }
            }
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Không thể xoá danh mục!");
        }

        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok().build();
    }

    public record ImageUploadResponse(String imageUrl, String objectPath) {
    }

    @PostMapping(value = "/admin/api/uploads/menu-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> uploadMenuImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new ValidationException("Vui lòng chọn ảnh!");
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new ValidationException("Kích thước ảnh tối đa 5MB!");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new ValidationException("File không hợp lệ (chỉ nhận ảnh)!");
            }

            String supabaseUrl = requiredEnv("SUPABASE_URL");
            String serviceKey = requiredEnv("SUPABASE_SERVICE_ROLE_KEY");
            String bucket = envOrDefault("SUPABASE_BUCKET", "coffee-shop-images");

            String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot).toLowerCase(Locale.ROOT);
                if (ext.length() > 10)
                    ext = "";
            }

            String objectPath = "products/" + "menu-" + UUID.randomUUID() + ext;
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new InternalServerException("Upload ảnh thất bại (Supabase)! ");
            }

            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
            return ResponseEntity.ok(new ImageUploadResponse(publicUrl, objectPath));
        } catch (ValidationException e) {
            throw e;
        } catch (InternalServerException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Upload ảnh thất bại!");
        }
    }

    public record MenuItemCreateRequest(String name, String categoryId, Double price, String imageUrl) {
    }

    public record MenuItemCreateResponse(String id, String name, String categoryId, double price, String imageUrl,
            String status) {
    }

    @PostMapping("/admin/api/menu-items")
    @ResponseBody
    public ResponseEntity<?> createMenuItem(@RequestBody MenuItemCreateRequest request) {
        try {
            if (request == null) {
                throw new ValidationException("Dữ liệu gửi lên không hợp lệ!");
            }

            String name = safeTrim(request.name());
            String categoryId = safeTrim(request.categoryId());
            Double priceObj = request.price();
            String imageUrl = safeTrim(request.imageUrl());

            if (name.isEmpty() || categoryId.isEmpty() || priceObj == null) {
                throw new ValidationException("Vui lòng nhập đầy đủ: Tên nước, Danh mục, Giá tiền!");
            }
            if (priceObj < 0) {
                throw new ValidationException("Giá tiền không hợp lệ!");
            }

            List<MenuItem> all = menuItemService.getAllMenuItems().get();
            if (all == null)
                all = List.of();

            for (MenuItem mi : all) {
                if (mi != null && mi.getName() != null && mi.getName().trim().equalsIgnoreCase(name)) {
                    throw new ConflictException("Tên món đã tồn tại!");
                }
            }

            String id = nextId("ITEM", all.stream().map(MenuItem::getId).toList());

            MenuItem item = new MenuItem();
            item.setId(id);
            item.setName(name);
            item.setCategoryId(categoryId);
            item.setPrice(priceObj);
            item.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
            item.setStatus("unavailable");
            item.setCreatedAt(LocalDateTime.now().toString());

            menuItemService.createMenuItem(item).get();

            return ResponseEntity.status(HttpStatus.CREATED).body(new MenuItemCreateResponse(
                    id,
                    name,
                    categoryId,
                    priceObj,
                    item.getImageUrl(),
                    item.getStatus()));
        } catch (ValidationException e) {
            throw e;
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Không thể thêm món!");
        }
    }

    @GetMapping("/admin/api/menu-items/{id}")
    @ResponseBody
    public ResponseEntity<?> getMenuItem(@PathVariable String id) {
        try {
            String trimmedId = safeTrim(id);
            if (trimmedId.isEmpty()) {
                throw new ValidationException("Mã món không hợp lệ!");
            }

            MenuItem item = menuItemService.getMenuItemById(trimmedId).get();
            if (item == null) {
                throw new NotFoundException("Không tìm thấy món!");
            }

            return ResponseEntity.ok(item);
        } catch (ValidationException e) {
            throw e;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Không thể tải món!");
        }
    }

    public record MenuItemUpdateRequest(String name, String categoryId, Double price, String imageUrl) {
    }

    public record MenuItemUpdateResponse(String id, String name, String categoryId, double price, String imageUrl,
            String status) {
    }

    @PutMapping("/admin/api/menu-items/{id}")
    @ResponseBody
    public ResponseEntity<?> updateMenuItem(@PathVariable String id, @RequestBody MenuItemUpdateRequest request) {
        try {
            String trimmedId = safeTrim(id);
            if (trimmedId.isEmpty() || request == null) {
                throw new ValidationException("Dữ liệu cập nhật không hợp lệ!");
            }

            MenuItem existing = menuItemService.getMenuItemById(trimmedId).get();
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy món!");
            }

            String name = safeTrim(request.name());
            String categoryId = safeTrim(request.categoryId());
            Double priceObj = request.price();
            String imageUrl = safeTrim(request.imageUrl());

            if (name.isEmpty() || categoryId.isEmpty() || priceObj == null) {
                throw new ValidationException("Vui lòng nhập đầy đủ: Tên nước, Danh mục, Giá tiền!");
            }
            if (priceObj < 0) {
                throw new ValidationException("Giá tiền không hợp lệ!");
            }

            List<MenuItem> all = menuItemService.getAllMenuItems().get();
            if (all != null) {
                for (MenuItem mi : all) {
                    if (mi == null || mi.getId() == null)
                        continue;
                    if (mi.getId().equals(trimmedId))
                        continue;
                    if (mi.getName() != null && mi.getName().trim().equalsIgnoreCase(name)) {
                        throw new ConflictException("Tên món đã tồn tại!");
                    }
                }
            }

            existing.setName(name);
            existing.setCategoryId(categoryId);
            existing.setPrice(priceObj);
            if (!imageUrl.isEmpty()) {
                existing.setImageUrl(imageUrl);
            }

            menuItemService.updateMenuItem(trimmedId, existing).get();

            return ResponseEntity.ok(new MenuItemUpdateResponse(
                    trimmedId,
                    existing.getName(),
                    existing.getCategoryId(),
                    existing.getPrice(),
                    existing.getImageUrl(),
                    existing.getStatus()));
        } catch (ValidationException e) {
            throw e;
        } catch (NotFoundException e) {
            throw e;
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Không thể cập nhật món!");
        }
    }

    @DeleteMapping("/admin/api/menu-items/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMenuItem(@PathVariable String id) {
        try {
            String trimmedId = safeTrim(id);
            if (trimmedId.isEmpty()) {
                throw new ValidationException("Mã món không hợp lệ!");
            }

            MenuItem existing = menuItemService.getMenuItemById(trimmedId).get();
            if (existing == null) {
                throw new NotFoundException("Không tìm thấy món!");
            }

            menuItemService.deleteMenuItem(trimmedId).get();
            return ResponseEntity.ok().build();
        } catch (ValidationException e) {
            throw e;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Không thể xoá món!");
        }
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String requiredEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.trim().isEmpty()) {
            throw new InternalServerException("Thiếu cấu hình " + key + " trong biến môi trường!");
        }
        return v.trim();
    }

    private static String envOrDefault(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v == null || v.trim().isEmpty())
            return defaultValue;
        return v.trim();
    }

    private static String nextId(String prefix, List<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            if (id == null || !id.startsWith(prefix))
                continue;
            String numberPart = id.substring(prefix.length());
            try {
                int n = Integer.parseInt(numberPart);
                if (n > max)
                    max = n;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%03d", max + 1);
    }
}
