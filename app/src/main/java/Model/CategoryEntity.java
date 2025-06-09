package Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryEntity {

    @SerializedName("CategoryName")
    private String categoryName;
    @SerializedName("Index")
    private int index;

    @SerializedName("List")
    private List<AnimationEntity> animationModelList;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<AnimationEntity> getAnimationModelList() {
        return animationModelList;
    }

    public void setAnimationModelList(List<AnimationEntity> animationModelList) {
        this.animationModelList = animationModelList;
    }

    public CategoryEntity(String categoryName, int index, List<AnimationEntity> animationModelList) {
        this.categoryName = categoryName;
        this.index = index;
        this.animationModelList = animationModelList;
    }
}
