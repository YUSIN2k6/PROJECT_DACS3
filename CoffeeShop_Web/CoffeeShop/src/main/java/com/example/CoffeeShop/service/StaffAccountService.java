package com.example.CoffeeShop.service;

import com.example.CoffeeShop.entity.Staff;
import com.example.CoffeeShop.entity.User;
import com.example.CoffeeShop.web.ConflictException;
import com.example.CoffeeShop.web.InternalServerException;
import com.example.CoffeeShop.web.NotFoundException;
import com.example.CoffeeShop.web.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StaffAccountService {
    private final StaffService staffService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public StaffAccountService(StaffService staffService, UserService userService, PasswordEncoder passwordEncoder) {
        this.staffService = staffService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public record CreateStaffCommand(
            String fullName,
            String phone,
            String email,
            String username,
            String password,
            String position,
            String status) {
    }

    public record UpdateStaffCommand(
            String fullName,
            String phone,
            String email,
            String username,
            String password,
            String position,
            String status) {
    }

    public record StaffDetail(
            String staffId,
            String fullName,
            String phone,
            String email,
            String position,
            String status,
            String userId,
            String username,
            String role) {
    }

    public record StaffUpsertResult(
            String staffId,
            String userId,
            String username,
            String role) {
    }

    public StaffUpsertResult createStaffWithUser(CreateStaffCommand command) {
        try {
            if (command == null) {
                throw new ValidationException("Dữ liệu gửi lên không hợp lệ!");
            }

            String fullName = safeTrim(command.fullName());
            String phone = safeTrim(command.phone());
            String email = safeTrim(command.email());
            String username = safeTrim(command.username());
            String password = safeTrim(command.password());
            String position = safeTrim(command.position());
            String status = safeTrim(command.status());

            if (fullName.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty() || position.isEmpty()
                    || status.isEmpty()) {
                throw new ValidationException(
                        "Vui lòng nhập đầy đủ: Họ tên, SĐT, Tên tài khoản, Mật khẩu, Chức vụ, Tình trạng!");
            }

            List<Staff> allStaff = staffService.getAllStaff().get();
            String staffId = nextId("STAFF",
                    allStaff == null ? List.of() : allStaff.stream().map(Staff::getId).toList());

            Staff staff = new Staff();
            staff.setId(staffId);
            staff.setName(fullName);
            staff.setPhone(phone);
            staff.setEmail(email.isEmpty() ? null : email);
            staff.setPosition(position);
            staff.setStatus(status);
            staff.setHireDate(LocalDate.now().toString());

            staffService.createStaff(staff).get();

            List<User> allUsers = userService.getAllUsers().get();
            String userId = nextId("USER", allUsers == null ? List.of() : allUsers.stream().map(User::getId).toList());

            String normalizedUsername = sanitizeUsername(username);
            if (normalizedUsername.equals("user") && !username.equalsIgnoreCase("user")) {
                staffService.deleteStaff(staffId).get();
                throw new ValidationException("Tên tài khoản không hợp lệ!");
            }
            if (usernameExists(normalizedUsername, allUsers == null ? List.of() : allUsers)) {
                staffService.deleteStaff(staffId).get();
                throw new ConflictException("Tên tài khoản đã tồn tại!");
            }

            String role = positionToRole(position);

            User user = new User();
            user.setId(userId);
            user.setUsername(normalizedUsername);
            user.setEmail(email.isEmpty() ? null : email);
            user.setRole(role);
            user.setStaffId(staffId);
            user.setPasswordHash(passwordEncoder.encode(password));

            try {
                userService.createUser(user).get();
            } catch (Exception userCreateError) {
                staffService.deleteStaff(staffId).get();
                throw userCreateError;
            }

            return new StaffUpsertResult(
                    staffId,
                    userId,
                    user.getUsername(),
                    role);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof ConflictException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();
            throw new InternalServerException("Không thể thêm nhân viên (Firebase lỗi hoặc dữ liệu không hợp lệ)!");
        }
    }

    public StaffDetail getStaffDetail(String staffId) {
        try {
            String id = safeTrim(staffId);
            if (id.isEmpty()) {
                throw new ValidationException("Mã nhân viên không hợp lệ!");
            }

            Staff staff = staffService.getStaffById(id).get();
            if (staff == null) {
                throw new NotFoundException("Không tìm thấy nhân viên!");
            }

            User user = userService.getUserByStaffId(id).get();
            String userId = user == null ? null : user.getId();
            String username = user == null ? null : user.getUsername();
            String role = user == null ? null : user.getRole();

            return new StaffDetail(
                    staff.getId(),
                    staff.getName(),
                    staff.getPhone(),
                    staff.getEmail(),
                    staff.getPosition(),
                    staff.getStatus(),
                    userId,
                    username,
                    role);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof NotFoundException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();
            throw new InternalServerException("Lỗi khi lấy chi tiết nhân viên!");
        }
    }

    public StaffUpsertResult updateStaffWithUser(String staffId, UpdateStaffCommand command) {
        try {
            String id = safeTrim(staffId);
            if (id.isEmpty() || command == null) {
                throw new ValidationException("Dữ liệu cập nhật không hợp lệ!");
            }

            String fullName = safeTrim(command.fullName());
            String phone = safeTrim(command.phone());
            String email = safeTrim(command.email());
            String username = safeTrim(command.username());
            String password = safeTrim(command.password());
            String position = safeTrim(command.position());
            String status = safeTrim(command.status());

            if (fullName.isEmpty() || phone.isEmpty() || username.isEmpty() || position.isEmpty() || status.isEmpty()) {
                throw new ValidationException("Vui lòng nhập đầy đủ: Họ tên, SĐT, Tên tài khoản, Chức vụ, Tình trạng!");
            }

            Staff existingStaff = staffService.getStaffById(id).get();
            if (existingStaff == null) {
                throw new NotFoundException("Không tìm thấy nhân viên!");
            }

            existingStaff.setName(fullName);
            existingStaff.setPhone(phone);
            existingStaff.setEmail(email.isEmpty() ? null : email);
            existingStaff.setPosition(position);
            existingStaff.setStatus(status);
            staffService.updateStaff(id, existingStaff).get();

            User user = userService.getUserByStaffId(id).get();
            if (user == null) {
                throw new NotFoundException("Không tìm thấy tài khoản nhân viên!");
            }

            String normalizedUsername = sanitizeUsername(username);
            if (normalizedUsername.equals("user") && !username.equalsIgnoreCase("user")) {
                throw new ValidationException("Tên tài khoản không hợp lệ!");
            }

            User conflict = userService.getUserByUsername(normalizedUsername).get();
            if (conflict != null && conflict.getId() != null && !conflict.getId().equals(user.getId())) {
                throw new ConflictException("Tên tài khoản đã tồn tại!");
            }

            String role = positionToRole(position);

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", normalizedUsername);
            updates.put("email", email.isEmpty() ? null : email);
            updates.put("role", role);
            updates.put("status", user.getStatus());
            if (!password.isEmpty()) {
                updates.put("password_hash", passwordEncoder.encode(password));
            }

            userService.updateUserFields(user.getId(), updates).get();

            return new StaffUpsertResult(
                    id,
                    user.getId(),
                    normalizedUsername,
                    role);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof NotFoundException || e instanceof ConflictException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();
            throw new InternalServerException("Không thể cập nhật nhân viên!");
        }
    }

    public record StaffDeleteResult(
            String staffId,
            String userId) {
    }

    public StaffDeleteResult deleteStaffWithUser(String staffId) {
        try {
            String id = safeTrim(staffId);
            if (id.isEmpty()) {
                throw new ValidationException("Mã nhân viên không hợp lệ!");
            }

            Staff staff = staffService.getStaffById(id).get();
            if (staff == null) {
                throw new NotFoundException("Không tìm thấy nhân viên!");
            }

            User user = userService.getUserByStaffId(id).get();
            String userId = user == null ? null : user.getId();

            if (userId != null && !userId.isBlank()) {
                userService.deleteUser(userId).get();
            }

            staffService.deleteStaff(id).get();

            return new StaffDeleteResult(id, userId);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof NotFoundException) {
                throw (RuntimeException) e;
            }
            e.printStackTrace();
            throw new InternalServerException("Không thể xoá nhân viên!");
        }
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
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

    private static String sanitizeUsername(String raw) {
        if (raw == null)
            return "user";
        String s = raw.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^a-z0-9._-]", "");
        if (s.isEmpty())
            return "user";
        return s;
    }

    private static boolean usernameExists(String username, List<User> existingUsers) {
        if (username == null)
            return true;
        for (User u : existingUsers) {
            if (u != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    private static String positionToRole(String position) {
        if (position == null)
            return "staffer";
        String p = position.trim().toLowerCase(Locale.ROOT);
        if (p.equals("quản trị viên") || p.equals("quan tri vien"))
            return "admin";
        if (p.equals("quản lý") || p.equals("quan ly") || p.equals("thu ngân") || p.equals("thu ngan"))
            return "cashier";
        return "staffer";
    }
}
