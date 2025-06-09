package Model;

import java.util.List;

public class RootModel {
    private List<CategoryEntity> categoryModelList;

    public List<CategoryEntity> getCategoryModelList() {
        return categoryModelList;
    }

    public void setCategoryModelList(List<CategoryEntity> categoryModelList) {
        this.categoryModelList = categoryModelList;
    }

    public RootModel(List<CategoryEntity> categoryModelList) {
        this.categoryModelList = categoryModelList;
    }
}
