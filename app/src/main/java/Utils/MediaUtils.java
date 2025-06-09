package Utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class MediaUtils {
    // Kiểm tra file trong thư mục Pictures/animation-raw/categoryName
    public static boolean isFileExistsInAppStorage(Context context, String name, String categoryName) {
        // Đường dẫn thư mục chứa file
        File directory = new File(
                context.getFilesDir(),
                "animation-raw/" + ModelUtils.toFolderName(categoryName)
        );


        // Tạo file name tương ứng
        File gifFile = new File(directory, name + ".gif");
        File mp4File = new File(directory, name + ".mp4");

        boolean gifExists = gifFile.exists();
        boolean mp4Exists = mp4File.exists();

        Log.d("DEBUG FILE", "GIF exists: " + gifExists + " | MP4 exists: " + mp4Exists);
        return gifExists || mp4Exists;
    }

    public static File getDownloadedAnimationFile(Context context, String name, String categoryName) {
        File directory = new File(
                context.getFilesDir(),
                "animation-raw/" + ModelUtils.toFolderName(categoryName)
        );

        File gifFile = new File(directory, name + ".gif");
        if (gifFile.exists()) {
            return gifFile;
        }

        File mp4File = new File(directory, name + ".mp4");
        if (mp4File.exists()) {
            return mp4File;
        }

        return null; // Không tìm thấy file
    }


}
