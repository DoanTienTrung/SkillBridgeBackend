package com.skillbridge.skillbridgebackend.Service;

import com.skillbridge.skillbridgebackend.exception.FileStorageException;
import com.skillbridge.skillbridgebackend.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    // Thư mục lưu trữ file audio
    private static final String UPLOAD_DIR = "uploads/audio/";
    
    // Thư mục lưu trữ file ảnh
    private static final String IMAGE_UPLOAD_DIR = "uploads/images/";
    
    // Các định dạng audio được phép
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp3", "wav", "m4a", "aac", "ogg");
    
    // Các định dạng ảnh được phép
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    
    // Kích thước file ảnh tối đa (10MB)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    
    // Kích thước file tối đa (50MB)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    // MIME types được phép
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/wave", 
        "audio/x-wav", "audio/mp4", "audio/aac", "audio/ogg"
    );
    
    // MIME types cho ảnh được phép
    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    /**
     * Lưu file audio và trả về đường dẫn
     * @param file MultipartFile cần lưu
     * @return Đường dẫn tương đối của file đã lưu
     * @throws RuntimeException nếu có lỗi trong quá trình lưu file
     */
    public String saveAudioFile(MultipartFile file) {
        try {
            // Validate file trước khi lưu
            if (!isValidAudioFile(file)) {
                throw new InvalidFileException("File không hợp lệ. Chỉ chấp nhận file audio MP3, WAV, M4A, AAC, OGG với kích thước tối đa 50MB.");
            }

            // Tạo thư mục nếu chưa tồn tại
            createDirectoryIfNotExists(UPLOAD_DIR);

            // Tạo tên file unique
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            
            // Đường dẫn đầy đủ để lưu file
            Path targetLocation = Paths.get(UPLOAD_DIR + uniqueFileName);

            // Copy file vào thư mục đích
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn tương đối
            return UPLOAD_DIR + uniqueFileName;

        } catch (IOException e) {
            throw new FileStorageException("Không thể lưu file audio: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa file audio theo tên file
     * @param fileName Tên file cần xóa (bao gồm cả đường dẫn tương đối)
     * @throws RuntimeException nếu có lỗi khi xóa file
     */
    public void deleteAudioFile(String fileName) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new InvalidFileException("Tên file không được để trống");
            }

            Path filePath = Paths.get(fileName);
            File file = filePath.toFile();

            if (file.exists()) {
                if (!file.delete()) {
                    throw new FileStorageException("Không thể xóa file: " + fileName);
                }
            } else {
                throw new FileStorageException("File không tồn tại: " + fileName);
            }

        } catch (FileStorageException | InvalidFileException e) {
            throw e; // Re-throw custom exceptions
        } catch (Exception e) {
            throw new FileStorageException("Lỗi khi xóa file audio: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra file audio có hợp lệ hay không
     * @param file File cần kiểm tra
     * @return true nếu file hợp lệ, false nếu không
     */
    public boolean isValidAudioFile(MultipartFile file) {
        // Kiểm tra file có null hoặc empty không
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Kiểm tra extension
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            return false;
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            return false;
        }

        // Kiểm tra MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        return true;
    }

    /**
     * Tạo tên file unique để tránh trùng lặp
     * @param originalFileName Tên file gốc
     * @return Tên file unique
     */
    private String generateUniqueFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new InvalidFileException("Tên file gốc không hợp lệ");
        }

        // Lấy extension của file
        String fileExtension = getFileExtension(originalFileName);
        
        // Tạo UUID unique
        String uniqueId = UUID.randomUUID().toString();
        
        // Lấy timestamp hiện tại
        long timestamp = System.currentTimeMillis();
        
        // Tạo tên file: timestamp_uuid.extension
        return timestamp + "_" + uniqueId + "." + fileExtension;
    }

    /**
     * Tạo thư mục nếu chưa tồn tại
     * @param directory Đường dẫn thư mục cần tạo
     */
    private void createDirectoryIfNotExists(String directory) {
        try {
            Path path = Paths.get(directory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new FileStorageException("Không thể tạo thư mục: " + directory, e);
        }
    }

    /**
     * Lấy extension của file từ tên file
     * @param fileName Tên file
     * @return Extension của file (không bao gồm dấu chấm)
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Lấy kích thước file dưới dạng chuỗi readable
     * @param bytes Kích thước file tính bằng bytes
     * @return Chuỗi mô tả kích thước file (VD: 1.5 MB)
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Kiểm tra file có tồn tại hay không
     * @param filePath Đường dẫn file
     * @return true nếu file tồn tại
     */
    public boolean fileExists(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Lấy thông tin file
     * @param filePath Đường dẫn file
     * @return Thông tin file
     */
    public FileInfo getFileInfo(String filePath) {
        try {
            if (!fileExists(filePath)) {
                return null;
            }

            Path path = Paths.get(filePath);
            File file = path.toFile();

            return new FileInfo(
                file.getName(),
                file.length(),
                formatFileSize(file.length()),
                getFileExtension(file.getName())
            );

        } catch (Exception e) {
            throw new FileStorageException("Không thể lấy thông tin file: " + e.getMessage(), e);
        }
    }

    /**
     * Lưu file ảnh và trả về đường dẫn
     */
    public String saveImageFile(MultipartFile file) {
        try {
            // Validate file trước khi lưu
            if (!isValidImageFile(file)) {
                throw new InvalidFileException("File không hợp lệ. Chỉ chấp nhận file ảnh JPG, PNG, GIF với kích thước tối đa 10MB.");
            }

            // Tạo thư mục nếu chưa tồn tại
            createDirectoryIfNotExists(IMAGE_UPLOAD_DIR);

            // Tạo tên file unique
            String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            
            // Đường dẫn đầy đủ để lưu file
            Path targetLocation = Paths.get(IMAGE_UPLOAD_DIR + uniqueFileName);

            // Copy file vào thư mục đích
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn tương đối
            return IMAGE_UPLOAD_DIR + uniqueFileName;

        } catch (IOException e) {
            throw new FileStorageException("Không thể lưu file ảnh: " + e.getMessage(), e);
        }
    }
    
    /**
     * Kiểm tra file ảnh có hợp lệ hay không
     */
    public boolean isValidImageFile(MultipartFile file) {
        // Kiểm tra file có null hoặc empty không
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_IMAGE_SIZE) {
            return false;
        }

        // Kiểm tra extension
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            return false;
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(fileExtension)) {
            return false;
        }

        // Kiểm tra MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        return true;
    }
    
    /**
     * Xóa file ảnh theo tên file
     */
    public void deleteImageFile(String fileName) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new InvalidFileException("Tên file không được để trống");
            }

            Path filePath = Paths.get(fileName);
            File file = filePath.toFile();

            if (file.exists()) {
                if (!file.delete()) {
                    throw new FileStorageException("Không thể xóa file: " + fileName);
                }
            } else {
                throw new FileStorageException("File không tồn tại: " + fileName);
            }

        } catch (FileStorageException | InvalidFileException e) {
            throw e; // Re-throw custom exceptions
        } catch (Exception e) {
            throw new FileStorageException("Lỗi khi xóa file ảnh: " + e.getMessage(), e);
        }
    }

    /**
     * Inner class để chứa thông tin file
     */
    public static class FileInfo {
        private final String fileName;
        private final long size;
        private final String formattedSize;
        private final String extension;

        public FileInfo(String fileName, long size, String formattedSize, String extension) {
            this.fileName = fileName;
            this.size = size;
            this.formattedSize = formattedSize;
            this.extension = extension;
        }

        // Getters
        public String getFileName() { return fileName; }
        public long getSize() { return size; }
        public String getFormattedSize() { return formattedSize; }
        public String getExtension() { return extension; }
    }
}