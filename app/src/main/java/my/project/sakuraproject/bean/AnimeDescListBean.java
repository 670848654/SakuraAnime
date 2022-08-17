package my.project.sakuraproject.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnimeDescListBean implements Serializable {
    // 番剧多季集合
    private List<AnimeDescRecommendBean> animeDescMultiBeans = new ArrayList<>();
    // 番剧推荐集合
    private List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
    // 播放列表集合
    private List<AnimeDramasBean> animeDramasBeans = new ArrayList<>();

    public List<AnimeDramasBean> getAnimeDramasBeans() {
        return animeDramasBeans;
    }

    public void setAnimeDramasBeans(List<AnimeDramasBean> animeDramasBeans) {
        this.animeDramasBeans = animeDramasBeans;
    }

    public List<AnimeDescRecommendBean> getAnimeDescMultiBeans() {
        return animeDescMultiBeans;
    }

    public void setAnimeDescMultiBeans(List<AnimeDescRecommendBean> animeDescMultiBeans) {
        this.animeDescMultiBeans = animeDescMultiBeans;
    }

    public List<AnimeDescRecommendBean> getAnimeDescRecommendBeans() {
        return animeDescRecommendBeans;
    }

    public void setAnimeDescRecommendBeans(List<AnimeDescRecommendBean> animeDescRecommendBeans) {
        this.animeDescRecommendBeans = animeDescRecommendBeans;
    }
}
