package Database;

import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Model.CategoryEntity;

public class DatabaseHelper {
    public interface OnListDataFetchedListener {
        void onDataFetched(List<CategoryEntity> categoryList);
        void onError(Exception e);
    }
    public static void fetchAllParents(OnListDataFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("DatabaseHelper", "Fetching data from Firestore...");

        db.collection("Ad1").document("json2")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("DatabaseHelper", "Document exists.");

                        Object rawJson = documentSnapshot.get("Category");

                        if (rawJson != null) {
                            try {
                                Gson gson = new Gson();

                                // Convert raw object to JSON string
                                String jsonString = gson.toJson(rawJson);
                                Log.d("DatabaseHelper", "Raw JSON string: " + jsonString);

                                // Deserialize JSON string to List<CategoryModel>
                                Type listType = new TypeToken<List<CategoryEntity>>() {}.getType();
                                List<CategoryEntity> categoryList = gson.fromJson(jsonString, listType);

                                // Check if list is null or empty
                                if (categoryList == null || categoryList.isEmpty()) {
                                    Log.w("DatabaseHelper", "Parsed list is null or empty.");
                                } else {
                                    Log.d("DatabaseHelper", "Parsed categoryList size: " + categoryList.size());
                                    for (CategoryEntity cm : categoryList) {
                                        Log.d("DatabaseHelper", "Category: " + cm.getCategoryName()
                                                + ", Animations: " + (cm.getAnimationModelList() != null ? cm.getAnimationModelList().size() : "null"));
                                    }
                                }

                                // Sort by index
                                Collections.sort(categoryList, Comparator.comparingInt(CategoryEntity::getIndex));
                                listener.onDataFetched(categoryList);
                            } catch (Exception e) {
                                Log.e("DatabaseHelper", "Parsing error: " + e.getMessage(), e);
                                listener.onError(e);
                            }
                        } else {
                            Log.w("DatabaseHelper", "Category field is null.");
                            listener.onDataFetched(new ArrayList<>());
                        }
                    } else {
                        Log.w("DatabaseHelper", "Document does not exist.");
                        listener.onDataFetched(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseHelper", "Error fetching document", e);
                    listener.onError(e);
                });
    }

}
