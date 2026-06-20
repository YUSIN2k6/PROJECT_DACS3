import os

# 1. Cấu hình giới hạn kích thước mỗi file (tính bằng số lượng ký tự)
# 300,000 ký tự (khoảng ~300KB) là mức rất lý tưởng. 
# AI sẽ đọc trơn tru, không bị quên trước quên sau và web không bị đơ.
MAX_CHARS_PER_FILE = 300000 

# 2. Lọc danh sách thư mục rác, thư viện, thư mục tự build (BỎ QUA)
EXCLUDE_DIRS = {
    '.gradle', '.idea', 'build', 'out', '.firebase',
    'node_modules', '.git', 'assets', 'gradle', 
    'drawable', 'mipmap', 'images' # Bỏ qua cả thư mục ảnh
}

# 3. Chỉ quét các đuôi file có chứa logic/giao diện
INCLUDE_EXTENSIONS = {
    # Code Android
    '.kt', '.xml', '.kts',
    # Code Web Admin
    '.html', '.css', '.js', '.php'
}

# 4. Những file đặc biệt nên bỏ qua (bảo mật hoặc file thư viện rác)
EXCLUDE_FILES = {
    'google-services.json', 'local.properties', 
    'package-lock.json', 'gradle-wrapper.properties'
}

OUTPUT_PREFIX = 'gom_code_part_'

def is_valid_file(filepath, filename):
    # Lọc file theo tên (các file cấm)
    if filename in EXCLUDE_FILES:
        return False
        
    # Lọc file theo đuôi (chỉ lấy code)
    _, ext = os.path.splitext(filepath)
    return ext.lower() in INCLUDE_EXTENSIONS

def generate_project_chunks():
    root_dir = os.getcwd()
    
    current_chunk_idx = 1
    current_content = []
    current_char_count = 0
    
    # Tạo Header cho file đầu tiên
    current_content.append(f"# TỔNG HỢP MÃ NGUỒN DỰ ÁN  (PHẦN {current_chunk_idx})\n")
    current_content.append("=" * 50 + "\n")

    for dirpath, dirnames, filenames in os.walk(root_dir):
        # Lọc bỏ thư mục rác (quan trọng: dùng slice assignment để sửa trực tiếp list của os.walk)
        dirnames[:] = [d for d in dirnames if d not in EXCLUDE_DIRS]
        
        for filename in filenames:
            filepath = os.path.join(dirpath, filename)
            
            if is_valid_file(filepath, filename):
                rel_path = os.path.relpath(filepath, root_dir)
                
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                        
                        file_block = f"\n\n{'='*50}\n### FILE: {rel_path}\n{'='*50}\n```\n{content}\n```\n"
                        block_len = len(file_block)
                        
                        # Nếu thêm file này vào mà bị quá tải -> Lưu file cũ, tạo file part mới
                        if current_char_count + block_len > MAX_CHARS_PER_FILE and current_char_count > 0:
                            save_chunk(current_chunk_idx, current_content)
                            
                            # Khởi tạo lại biến cho file part tiếp theo
                            current_chunk_idx += 1
                            current_content = [f"# TỔNG HỢP MÃ NGUỒN DỰ ÁN  (PHẦN {current_chunk_idx})\n" + "=" * 50 + "\n"]
                            current_char_count = len(current_content[0])
                            
                        current_content.append(file_block)
                        current_char_count += block_len
                        
                except Exception as e:
                    print(f"⚠️ Bỏ qua file do không thể đọc dưới dạng văn bản: {rel_path}")

    # Lưu phần code cuối cùng còn dư (nếu có)
    if current_content and current_char_count > len(current_content[0]):
        save_chunk(current_chunk_idx, current_content)

def save_chunk(idx, content_list):
    filename = f"{OUTPUT_PREFIX}{idx}.txt"
    with open(filename, 'w', encoding='utf-8') as out_f:
        out_f.write("".join(content_list))
    print(f"✅ Đã tạo file: {filename}")

if __name__ == "__main__":
    print("⏳ Đang quét và chia nhỏ mã nguồn...")
    # Xóa các file part cũ đi (nếu có) để tránh dữ liệu bị trùng lặp
    for f in os.listdir('.'):
        if f.startswith(OUTPUT_PREFIX) and f.endswith('.txt'):
            os.remove(f)
            
    generate_project_chunks()
    print("🎉 Hoàn tất!")