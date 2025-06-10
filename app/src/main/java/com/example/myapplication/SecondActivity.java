package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import Adapter.CategoryAdapter;
import Database.DatabaseHelper;
import Model.CategoryEntity;


public class SecondActivity extends AppCompatActivity {
    private RecyclerView rcParent;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        rcParent = findViewById(R.id.rcParent);

        ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean shouldReload = result.getData().getBooleanExtra("shouldReload", false);
                        if (shouldReload) {
                            reloadData();
                        }
                    }
                }
        );


        categoryAdapter = new CategoryAdapter(this, new ArrayList<>(), (cat, name, url) -> {
            Intent intent = new Intent(SecondActivity.this, DetailActivity.class);
            intent.putExtra("categoryName", cat);
            intent.putExtra("name", name);
            intent.putExtra("animationUrl", url);
            detailLauncher.launch(intent);
        });


        // Đăng ký sự kiện "See All"
        categoryAdapter.setOnSeeAllClickListener((categoryName, childList) -> {
            Log.d("SeeAllClick", "Category: " + categoryName);
            Log.d("SeeAllClick", "Child list: " + new Gson().toJson(childList));

            Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);
            intent.putExtra("categoryName", categoryName);
            intent.putExtra("childListJson", new Gson().toJson(childList));
            thirdActivityLauncher.launch(intent);
        });
        rcParent.setLayoutManager(new LinearLayoutManager(this));
        rcParent.setAdapter(categoryAdapter);

        DatabaseHelper.fetchAllParents(new DatabaseHelper.OnListDataFetchedListener() {
            @Override
            public void onDataFetched(List<CategoryEntity> categoryList) {
                categoryAdapter.updateData(categoryList);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SecondActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void reloadData() {
        DatabaseHelper.fetchAllParents(new DatabaseHelper.OnListDataFetchedListener() {
            @Override
            public void onDataFetched(List<CategoryEntity> categoryList) {
                categoryAdapter.updateData(categoryList);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SecondActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ActivityResultLauncher<Intent> thirdActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean shouldReload = result.getData().getBooleanExtra("shouldReload2", false);
                    if (shouldReload) {
                        reloadData();
                    }
                }
            }
    );


}