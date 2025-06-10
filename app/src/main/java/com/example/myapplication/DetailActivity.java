package com.example.myapplication;

import static Utils.MediaUtils.getDownloadedAnimationFile;
import static Utils.MediaUtils.isFileExistsInAppStorage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import Helper.ExoPlayerHelper;
import Helper.PreferencesHelper;
import Utils.ModelUtils;

public class DetailActivity extends AppCompatActivity {
    TextView tvBack;
    String animationUrl, name, categoryName;
    Button btnDownload, btnApply;
    View loadingLayout;
    RelativeLayout layoutMainContent;
    String currentAnimationName;
    ImageView imageView;
    private PlayerView playerView;
    private ExoPlayerHelper exoPlayerHelper;

    ImageView imgLoad;

    @UnstableApi
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        name = getIntent().getStringExtra("name");
        Log.d("DEBUG", "name = " + name);
        categoryName = getIntent().getStringExtra("categoryName");
        Log.d("DEBUG", "Category Name = " +categoryName);
        animationUrl = getIntent().getStringExtra("animationUrl");
        Log.d("DEBUG", "Url = " +animationUrl);


        tvBack = findViewById(R.id.tvBackDetail);
        tvBack.setOnClickListener(v -> {
            Log.d("DetailActivity", "tvBack clicked, returning downloadedFileName = " + name);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("downloadedFileName", name);
            resultIntent.putExtra("shouldReload", true);

            setResult(RESULT_OK, resultIntent);
            finish();

        });
        imgLoad = findViewById(R.id.my_image_view);
        imageView = findViewById(R.id.imgGifDetail);
        btnDownload = findViewById(R.id.btnDownloadGif);
        btnApply = findViewById(R.id.btnApply);

        loadingLayout = findViewById(R.id.loading_layout);
        layoutMainContent = findViewById(R.id.layoutMainContent);
        playerView = findViewById(R.id.playerView);

        exoPlayerHelper = new ExoPlayerHelper(this);
        playerView.setPlayer(exoPlayerHelper.getPlayer());
        playerView.setUseController(false);

        btnApply.setVisibility(View.GONE);
        btnDownload.setVisibility(View.GONE);

        // Khi bắt đầu tải (hiện loading)
        loadingLayout.setVisibility(View.VISIBLE);

        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        imgLoad.startAnimation(rotate);

        // Load từ bộ nhớ cục bộ nếu tồn tại file gif hay mp4 từ bộ nhớ cục bộ
        File file = getDownloadedAnimationFile(this, name, categoryName);

        if (file != null) {

            if (file.getName().endsWith(".gif")) {
                // Hiển thị GIF
                exoPlayerHelper.release();
                playerView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                Glide.with(DetailActivity.this)
                        .asGif()
                        .load(file)
                        .into(imageView);

                loadingLayout.setVisibility(View.GONE);
                layoutMainContent.setVisibility(View.VISIBLE);
                imgLoad.clearAnimation();
                imgLoad.setVisibility(View.GONE);
                btnApply.setVisibility(View.VISIBLE);
                btnDownload.setVisibility(View.GONE);
//                updateApplyButton(new PreferencesHelper(this).getAppliedAnimationName());
            }else if (file.getName().endsWith(".mp4")) {
                // Hiển thị video MP4
                Glide.with(DetailActivity.this).clear(imageView);
                imageView.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);
                playMp4WithExo(Uri.fromFile(file));
            }

        } else {
            // Không có file cục bộ → load từ internet
            if (animationUrl != null && !animationUrl.trim().isEmpty()) {
                Glide.with(this)
                        .asGif()
                        .load(animationUrl)
                        .listener(new RequestListener<GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e,
                                                        Object model,
                                                        Target<GifDrawable> target,
                                                        boolean isFirstResource) {
                                Log.e("GlideError", "GIF load failed, fallback to MP4", e);
                                playMp4WithExo(Uri.parse(animationUrl));
                                return true;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource,
                                                           Object model,
                                                           Target<GifDrawable> target,
                                                           DataSource dataSource,
                                                           boolean isFirstResource) {
                                loadingLayout.setVisibility(View.GONE);
                                layoutMainContent.setVisibility(View.VISIBLE);
                                imgLoad.clearAnimation();
                                imgLoad.setVisibility(View.GONE);

                                updateDownloadApplyButtons(DetailActivity.this, name, categoryName, btnApply, btnDownload);
                                updateApplyButton();
                                return false;
                            }
                        })
                        .into(imageView);
            }
            else {
                // Không có gif → fallback luôn sang mp4 online
                playMp4WithExo(Uri.parse(animationUrl));
            }
        }


        btnDownload.setOnClickListener(v -> {
            Log.d("Download", "Button clicked");


            if (animationUrl != null && !animationUrl.isEmpty()) {
                if (!isFileExistsInAppStorage(this, name, categoryName)) {
                    Log.d("Download", "Downloading File: " + animationUrl);
                    downloadFileToAppStorage(categoryName, name, animationUrl);
                } else {
                    Log.d("Download", "File already exists");
                }
            }
            else {
                Toast.makeText(this, "Không có file nào cần tải", Toast.LENGTH_SHORT).show();
            }
        });

        PreferencesHelper appPrefs = new PreferencesHelper(this);

        currentAnimationName = name;
        Log.d("DetailActivity", "currentName = " + currentAnimationName);
        btnApply.setOnClickListener(v -> {
            if (isFileExistsInAppStorage(this, name, categoryName)) {
                String appliedId = appPrefs.getAppliedAnimationName();
                String appliedCategory = appPrefs.getAppliedCategoryName();

                if (appliedId != null && appliedId.equals(currentAnimationName) &&
                        appliedCategory != null && appliedCategory.equals(ModelUtils.toFolderName(categoryName))) {
                    Toast.makeText(this, "Đã áp dụng ảnh này rồi", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ChargingActivity.class));
                } else {
                    // Lưu dữ liệu mới vào SharedPreferences
                    appPrefs.setAppliedAnimation(name, ModelUtils.toFolderName(categoryName));
                    Toast.makeText(this, "Áp dụng thành công!", Toast.LENGTH_SHORT).show();
                    updateApplyButton();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("shouldReload", true);
                    resultIntent.putExtra("downloadedFileName", name);
                    setResult(RESULT_OK, resultIntent);setResult(RESULT_OK);
                    startActivity(new Intent(this, ChargingActivity.class));
                }

            } else {
                Toast.makeText(this, "GIF/MP4 chưa được tải, không thể áp dụng!", Toast.LENGTH_SHORT).show();
            }
        });


    }
    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("downloadedFileName", name); // nếu cần
        resultIntent.putExtra("shouldReload", true);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed(); // gọi để quay lại
    }

    private boolean isFirstPlay = true;

    @UnstableApi
    private void playMp4WithExo(Uri mp4Url) {
        // Reset cờ mỗi lần phát mới
        isFirstPlay = true;

        // Ẩn ảnh tĩnh
        imageView.setVisibility(View.GONE);


        // Hiện PlayerView và loading
        playerView.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        imgLoad.setVisibility(View.VISIBLE);
        layoutMainContent.setVisibility(View.GONE);

        // Giải phóng exoPlayer cũ nếu có
        exoPlayerHelper.release();

        // Khởi tạo mới
        exoPlayerHelper = new ExoPlayerHelper(this);
        playerView.setPlayer(exoPlayerHelper.getPlayer());
        playerView.setUseController(false);

       exoPlayerHelper.prepareAndPlay(mp4Url);

        // Lắng nghe thay đổi trạng thái player
        exoPlayerHelper.getPlayer().addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        if (isFirstPlay) {
                            loadingLayout.setVisibility(View.VISIBLE);
                            imgLoad.setVisibility(View.VISIBLE);
                            layoutMainContent.setVisibility(View.GONE);
                        }
                        break;
                    case Player.STATE_READY:
                        loadingLayout.setVisibility(View.GONE);
                        imgLoad.setVisibility(View.GONE);
                        layoutMainContent.setVisibility(View.VISIBLE);
                        isFirstPlay = false; // Chỉ loading lần đầu
                        break;
                    case Player.STATE_ENDED:
                        // Không cần xử lý do đã setRepeatMode
                        break;
                }
            }
        });
        updateDownloadApplyButtons(DetailActivity.this, name, categoryName, btnApply, btnDownload);
        updateApplyButton();
    }

    private void updateDownloadApplyButtons(Context context, String name, String categoryName, Button btnApply, Button btnDownload) {
        if (!isFileExistsInAppStorage(context, name, categoryName)) {
            btnApply.setVisibility(View.GONE);
            btnDownload.setVisibility(View.VISIBLE);
        } else {
            btnApply.setVisibility(View.VISIBLE);
            btnDownload.setVisibility(View.GONE);
        }
    }

    private void downloadFileToAppStorage(String categoryName, String name, String animationUrl) {
        new Thread(() -> {
            try {
                Log.d("DownloadFile", "Bắt đầu tải từ URL: " + animationUrl);
                URL url = new URL(animationUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d("DownloadFile", "Mã phản hồi HTTP: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Lỗi kết nối: " + responseCode);
                }

                InputStream input = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                Log.d("DownloadFile", "Đang đọc dữ liệu...");
                while ((bytesRead = input.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, bytesRead);
                }

                byte[] fileBytes = byteBuffer.toByteArray();
                input.close();
                byteBuffer.close();
                Log.d("DownloadFile", "Tải xong. Kích thước file: " + fileBytes.length + " bytes");

                runOnUiThread(() -> saveToAppStorage(fileBytes, categoryName, name, animationUrl));

            } catch (Exception e) {
                Log.e("DownloadFile", "Lỗi khi tải file", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Tải file thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    //B1: View > Tool Windows > Device Explorer (LƯU Ý: Trên cây thư mục, chọn đúng thiết bị/emulator)

    //B2: Truy cập theo đường dẫn trên để coi file đã tải về ở bộ nhớ trong:
    // sdcard/Android/data/com.example.myapplication/files/Pictures/
    private void saveToAppStorage(byte[] fileBytes, String categoryName, String name, String animationUrl) {
        String fileName;
        if (animationUrl.endsWith(".mp4")) {
            fileName = name + ".mp4";
        } else if (animationUrl.endsWith(".gif")) {
            fileName = name + ".gif";
        } else {
            Toast.makeText(this, "Không có URL hợp lệ để lưu", Toast.LENGTH_SHORT).show();
            return;
        }
        String cat = ModelUtils.toFolderName(categoryName);

        // Mục tiêu: /Pictures/animation-raw/<categoryName>/<name> + đuôi file
        File appDir = new File(getFilesDir(), "animation-raw/" + cat);

        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        File outFile = new File(appDir, fileName);
        Log.d("SaveFile", "File sẽ được lưu tại: " + outFile.getAbsolutePath());

        try {
            FileOutputStream outputStream = new FileOutputStream(outFile);
            outputStream.write(fileBytes);
            outputStream.flush();
            outputStream.close();

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã lưu file vào bộ nhớ ứng dụng", Toast.LENGTH_SHORT).show();
                btnDownload.setVisibility(View.GONE);
                btnApply.setVisibility(View.VISIBLE);
//                updateApplyButton(new PreferencesHelper(this).getAppliedAnimationName());

            });
        } catch (IOException e) {
            Log.e("SaveFile", "Lỗi khi lưu file", e);
            runOnUiThread(() -> Toast.makeText(this, "Lỗi khi lưu file", Toast.LENGTH_SHORT).show());
        }
    }
    private void updateApplyButton() {
        String appliedId = new PreferencesHelper(this).getAppliedAnimationName();

        if (appliedId != null && appliedId.equals(currentAnimationName)) {
            btnApply.setText("Applied");
            btnApply.setEnabled(true);
        } else {
            btnApply.setText("Apply");
            btnApply.setEnabled(true);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayerHelper != null) {
            exoPlayerHelper.release();
            exoPlayerHelper = null;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
      //  sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        updateApplyButton();
    }
}