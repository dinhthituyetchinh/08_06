package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import Adapter.AnimationAdapter;
import Model.AnimationEntity;

public class ThirdActivity extends AppCompatActivity {
    AnimationAdapter adapter;
    List<AnimationEntity> childList;
    RecyclerView recyclerView;
    private TextView tvBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        recyclerView = findViewById(R.id.rcFullList);
        tvBack = findViewById(R.id.tvBack);

        tvBack.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("shouldReload2", true); // flag báo cần reload
            setResult(RESULT_OK, resultIntent);
            finish();
        });


        ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        String downloadedFileName = result.getData().getStringExtra("downloadedFileName");
                        // TODO: Load lại dữ liệu tương ứng
                        Log.d("ThirdActivity", "Received downloadedFileName = " + downloadedFileName);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            recyclerView.setAdapter(null);
                            recyclerView.setAdapter(adapter);
                        }, 300);

                    }
                }
        );

        String categoryName = getIntent().getStringExtra("categoryName"); //categoryName
        Log.d("ThirdActivity", "Received Title: " + categoryName);
        String json = getIntent().getStringExtra("childListJson");
        Log.d("ThirdActivity", "Received childListJson: " + json);

        if (json == null || json.isEmpty()) {
            finish();
            return;
        }

        Type listType = new TypeToken<List<AnimationEntity>>() {}.getType();
        childList = new Gson().fromJson(json, listType);
        Log.d("ThirdActivity", "Parsed childList size: " + (childList != null ? childList.size() : 0));

        //  Tạo adapter

        adapter = new AnimationAdapter(
                this,
                childList,
                categoryName,
                (cat, name, url) -> {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra("categoryName", cat);
                    intent.putExtra("name", name);
                    intent.putExtra("animationUrl", url);
                    detailLauncher.launch(intent);
                }
        );

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
    }

    private void reloadDataFromSource() {
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(adapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        reloadDataFromSource(); // force reload mỗi khi quay lại
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("shouldReload", true);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

}