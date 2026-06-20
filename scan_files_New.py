import os
from datetime import datetime

def get_size_format(b, factor=1024, suffix="B"):
    for unit in ["", "KB", "MB", "GB", "TB", "PB"]:
        if b < factor:
            return f"{b:.2f}{unit}{suffix}"
        b /= factor
    return f"{b:.2f}PB{suffix}"

def scan_directory():
    current_dir = os.getcwd()
    
    time_str = datetime.now().strftime("%d-%m-%Y_%Hh%Mp")
    output_file = f"Danh_sach_file_{time_str}.txt"
    
    # 1. BỘ LỌC: Bỏ qua các thư mục rác/build của hệ thống để tránh báo cáo ảo
    EXCLUDE_DIRS = {'.gradle', '.idea', 'build', 'out', '.firebase', 'node_modules', '.git', 'captures'}
    
    for f_name in os.listdir('.'):
        if f_name.startswith("Danh_sach_file_") and f_name.endswith(".txt"):
            try:
                os.remove(f_name)
            except:
                pass
                
    with open(output_file, "w", encoding="utf-8") as f:
        print("Đang quét file, vui lòng đợi...")
        
        for root, dirs, files in os.walk(current_dir):
            # Cắt tỉa các thư mục rác khỏi quá trình quét
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
            
            # 2. PHÁT HIỆN THƯ MỤC RỖNG: Không có folder con và không có file
            if not dirs and not files:
                relative_path = os.path.relpath(root, current_dir)
                f.write(f"{relative_path}\\ (Thư mục trống)\n")
                continue
                
            for filename in files:
                if filename.startswith("Danh_sach_file") or filename.startswith("gom_code") or filename.endswith(".py"):
                    continue
                
                filepath = os.path.join(root, filename)
                
                try:
                    file_size = os.path.getsize(filepath)
                    formatted_size = get_size_format(file_size)
                    relative_path = os.path.relpath(filepath, current_dir)
                    f.write(f"{relative_path} (Size: {formatted_size})\n")
                except Exception as e:
                    print(f"Khong the doc file: {filename} - Loi: {e}")

    print(f"Xong! Đã tạo file '{output_file}'")

if __name__ == "__main__":
    scan_directory()
    input("Nhấn Enter để thoát...")