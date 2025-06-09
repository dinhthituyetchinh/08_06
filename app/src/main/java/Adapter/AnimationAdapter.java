package Adapter;

import static Utils.MediaUtils.getDownloadedAnimationFile;
import static Utils.MediaUtils.isFileExistsInAppStorage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.DetailActivity;
import com.example.myapplication.R;

import java.io.File;
import java.util.List;

import Helper.ExoPlayerHelper;
import Helper.FileExtension;
import Helper.PreferencesHelper;
import Model.AnimationEntity;
import Utils.ModelUtils;


public class AnimationAdapter extends RecyclerView.Adapter<AnimationAdapter.ChildViewHolder> {
    public interface OnChildClickListener {
        void onClick(String categoryName, String name, String animationUrl);
    }
    private OnChildClickListener listener;

    private List<AnimationEntity> animationList;
    private String categoryName;

    private Context context;

    public AnimationAdapter(Context context, List<AnimationEntity> animationList, String categoryName, OnChildClickListener listener) {
        this.context = context;
        this.animationList = animationList;
        this.categoryName = categoryName;
        this.listener = listener;
    }

    @UnstableApi
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animation, parent, false);
        return new ChildViewHolder(view, parent.getContext());
    }


    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        AnimationEntity animationModel = animationList.get(position);
        //Xay thumb theo category
        String thumbUrl = ModelUtils.buildUrl(ModelUtils.THUMB_FOLDER, categoryName, animationModel.getName()) + ".jpg";
        Log.d("AnimationAdapter", "ThumbUrl "+thumbUrl);
        //Xay animation theo category
        String animationUrl;
        //Viet ham kiem tra duoi cua animation de tao url theo do
        if(FileExtension.isMp4Extension(animationModel.getAnimation()))
        {
            animationUrl = ModelUtils.buildUrl(ModelUtils.ANIMATION_FOLDER, categoryName, animationModel.getName()) + ".mp4";

        }else
        {
            animationUrl = ModelUtils.buildUrl(ModelUtils.ANIMATION_FOLDER, categoryName, animationModel.getName()) + ".gif";
        }

        //Sử dụng khi lưu vào bộ nhớ trong (bộ nhớ ứng dụng)
        if (isFileExistsInAppStorage(context, animationModel.getName(), categoryName)) {
            File file = getDownloadedAnimationFile(context, animationModel.getName(), categoryName);
                if (file.getName().endsWith(".mp4"))
                {
                    // Hiển thị video
                    holder.imgChild.setVisibility(View.GONE);
                    holder.playerView.setVisibility(View.VISIBLE);
                    // Hiển thị video MP4
                    holder.exoPlayerHelper.prepareAndPlay(Uri.fromFile(file));
                }
                else if(file.getName().endsWith(".gif"))
                {
                    // Hiển thị ảnh động GIF
                    holder.playerView.setVisibility(View.GONE);
                    holder.imgChild.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .asGif()
                            .placeholder(R.mipmap.loading)
                            .load(file)
                            .into(holder.imgChild);

                }
        } else {
            // Hiển thị ảnh tĩnh từ URL
            holder.playerView.setVisibility(View.GONE);
            holder.imgChild.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(thumbUrl)
                    .placeholder(R.mipmap.loading)
                    .into(holder.imgChild);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(categoryName, animationModel.getName(), animationUrl);
            }
        });
        // Kiểm tra trạng thái tải xuống
        boolean isDownloaded = isFileExistsInAppStorage(context, animationModel.getName(), categoryName);
        Log.d("StatusCheck", "isDownloaded: " + isDownloaded);

        // Lấy thông tin animation đang apply từ SharedPreferences
        PreferencesHelper prefsHelper = new PreferencesHelper(context);
        String appliedName = prefsHelper.getAppliedAnimationName();
        String appliedCategory = prefsHelper.getAppliedCategoryName();

        // Log giá trị so sánh
        Log.d("StatusCheck", "animationModel.getName(): " + animationModel.getName());
        Log.d("StatusCheck", "categoryName: " + categoryName);
        Log.d("StatusCheck", "categoryName2: " + ModelUtils.toFolderName(categoryName));
        Log.d("StatusCheck", "appliedName: " + appliedName);
        Log.d("StatusCheck", "appliedCategory: " + appliedCategory);

        // So sánh để xác định xem animation hiện tại có đang được apply hay không
        boolean isApplied = animationModel.getName().equals(appliedName)
                && ModelUtils.toFolderName(categoryName).equals(appliedCategory);
        Log.d("StatusCheck", "isApplied: " + isApplied);

        // Cập nhật trạng thái icon
        if (!isDownloaded) {
            // Chưa tải => hiện nút download
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setImageResource(R.mipmap.download);
            Log.d("StatusCheck", "Status: show download icon");

        } else if (isDownloaded && !isApplied) {
            // Đã tải, chưa apply => ẩn icon status
            holder.status.setVisibility(View.GONE);
            Log.d("StatusCheck", "Status: hide icon (downloaded, not applied)");

        } else if (isDownloaded && isApplied) {
            // Đã tải và đã apply => hiện icon tick
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setImageResource(R.mipmap.ic_checked);
            Log.d("StatusCheck", "Status: show check icon");

        }


    }

    @Override
    public int getItemCount() {
        return animationList != null ? animationList.size() : 0;
    }
    @Override
    public void onViewRecycled(@NonNull ChildViewHolder holder) {
        super.onViewRecycled(holder);
        holder.exoPlayerHelper.release();
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull ChildViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.exoPlayerHelper.release();
    }
    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        ImageView imgChild, status;
        PlayerView playerView;
        ExoPlayerHelper exoPlayerHelper;

        @UnstableApi
        public ChildViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            imgChild = itemView.findViewById(R.id.imgItemChild);
            playerView = itemView.findViewById(R.id.playerView);
            status = itemView.findViewById(R.id.status);
            exoPlayerHelper = new ExoPlayerHelper(context);
            playerView.setPlayer(exoPlayerHelper.getPlayer());
        }
    }

}