package Model;

public class AnimationEntity {
    private String name;
    private String animation;
    private String thumb;


    public AnimationEntity(String animation, String name) {
        this.animation = animation;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnimation() {
        if (name == null || animation == null) {
            return null;
        }
        return animation;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
    }

    public String getThumb() {
        if (name == null) {
            return null;
        }
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
