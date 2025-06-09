package Helper;

import android.content.Context;
import android.content.SharedPreferences;

import Utils.MediaUtils;
import Utils.ModelUtils;

public class PreferencesHelper {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_APPLIED_ID = "applied_id";
    private static final String KEY_APPLIED_CATEGORY = "applied_category";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setAppliedAnimation(String name, String category) {
        editor.putString(KEY_APPLIED_ID, name);
        editor.putString(KEY_APPLIED_CATEGORY, category);
        editor.apply();
    }

    public String getAppliedAnimationName() {
        return prefs.getString(KEY_APPLIED_ID, null);
    }

    public String getAppliedCategoryName() {
        return prefs.getString(KEY_APPLIED_CATEGORY, "default");
    }
    public String getAppliedFullPath() {
        String name = getAppliedAnimationName();
        String category = getAppliedCategoryName();
        if (name != null && category != null) {
            return category + "/" + name;  // ví dụ: cc/1.mp4
        }
        return null;
    }
}
