package Helper;

public class FileExtension {
    // Hàm lấy đuôi file (nếu không có thì trả về empty string)
    public static String getFileExtension(String filenameOrUrl) {
        if (filenameOrUrl == null) return "";

        int lastDot = filenameOrUrl.lastIndexOf('.');
        if (lastDot == -1) return "";  // Không có dấu chấm

        return filenameOrUrl.substring(lastDot + 1).toLowerCase();
    }

    // Hàm kiểm tra đuôi có phải mp4 không
    public static boolean isMp4Extension(String filenameOrUrl) {
        return "mp4".equals(getFileExtension(filenameOrUrl));
    }
}
