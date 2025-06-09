package Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import Model.AnimationEntity;
import Model.CategoryEntity;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ParentViewHolder> {

    private List<CategoryEntity> categoryList;
    private Context context;
    public interface OnSeeAllClickListener {
        void onSeeAllClicked(String categoryName, List<AnimationEntity> childList);
    }
    private OnSeeAllClickListener seeAllClickListener;

    public void setOnSeeAllClickListener(OnSeeAllClickListener listener) {
        this.seeAllClickListener = listener;
    }
    public interface OnChildClickListener {
        void onClick(String categoryName, String name, String animationUrl);
    }
    private OnChildClickListener childClickListener;

    public void setOnChildClickListener(OnChildClickListener listener) {
        this.childClickListener = listener;
    }

    public CategoryAdapter(Context context, List<CategoryEntity> categoryList, OnChildClickListener listener) {
        this.context = context;
        this.categoryList = categoryList != null ? categoryList : new ArrayList<>();
        this.childClickListener = listener;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder holder, int position) {
        if (categoryList == null || position >= categoryList.size()) return;

        CategoryEntity categoryModel = categoryList.get(position);
        String categoryName = categoryModel.getCategoryName();
        List<AnimationEntity> animationModels = categoryModel.getAnimationModelList();

        holder.titleParent.setText(categoryName);

        if (animationModels == null || animationModels.isEmpty()) {
            holder.rcChild.setVisibility(View.GONE);
        } else {
            holder.rcChild.setVisibility(View.VISIBLE);

            List<AnimationEntity> limitedList = animationModels.size() > 3
                    ? animationModels.subList(0, 3)
                    : animationModels;

            AnimationAdapter adapter = new AnimationAdapter(
                    holder.itemView.getContext(),
                    limitedList,
                    categoryName,
                    (cat, name, url) -> {
                        if (childClickListener != null) {
                            childClickListener.onClick(cat, name, url);  // → Gọi callback CategoryAdapter đang giữ
                        }
                    }
            );

            // Chỉ gọi callback, không startActivity trực tiếp
            holder.seeAllTextView.setOnClickListener(v -> {
                // Ghi log để kiểm tra thông tin khi người dùng nhấn "See All"
                Log.d("SeeAllClick", "Category: " + categoryName
                        + ", childList size: " + (animationModels != null ? animationModels.size() : 0));
                Log.d("SeeAllClick", "childList (JSON): " + new Gson().toJson(animationModels));

                if (seeAllClickListener != null) {
                    seeAllClickListener.onSeeAllClicked(categoryName, animationModels);
                }
            });


            holder.rcChild.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.rcChild.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public void updateData(List<CategoryEntity> newList) {
        if (newList != null) {
            if (this.categoryList == null) {
                this.categoryList = new ArrayList<>();
            }
            this.categoryList.clear();
            this.categoryList.addAll(newList);
            notifyDataSetChanged();
        }
    }

    public static class ParentViewHolder extends RecyclerView.ViewHolder {
        TextView titleParent, seeAllTextView;
        RecyclerView rcChild;

        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleParent = itemView.findViewById(R.id.titleParent);
            seeAllTextView = itemView.findViewById(R.id.seeAll);
            rcChild = itemView.findViewById(R.id.rcChild);
        }
    }
}
