package Utils;

import android.net.Uri;

public class ModelUtils {
    public static final String THUMB_FOLDER = "animation-raw/thumb";
    public static final String ANIMATION_FOLDER = "animation-raw/animation";
    private static final String SUPABASE_STORAGE_PREFIX =
            "https://owoyjgndxfvoyzkcbndm.supabase.co/storage/v1/object/public/";

    public static String buildUrl(String baseFolder, String categoryName, String fileName) {
        if (categoryName == null || fileName == null) return null;

        String folder = toFolderName(categoryName);
        String objectPath = baseFolder + "/" + folder + "/" + fileName;

//        // encode toàn bộ object path, bao gồm cả dấu /
//        String encodedObjectPath = Uri.encode(objectPath, null);

        return SUPABASE_STORAGE_PREFIX + objectPath;
    }


    public static String toFolderName(String name) {
        if (name == null) return null;
        // 1. chuyển về chữ thường
        String folder = name.toLowerCase();

        // 2. thay tất cả các ký tự không phải chữ số, chữ cái bằng dấu gạch ngang
        folder = folder.replaceAll("[^a-z0-9]+", "-");

        // 3. xóa dấu gạch ngang đầu và cuối
        folder = folder.replaceAll("^-+", "").replaceAll("-+$", "");

        return folder;
    }
}
