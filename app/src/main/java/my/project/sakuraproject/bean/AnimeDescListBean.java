package my.project.sakuraproject.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnimeDescListBean implements Serializable {
    // 播放列表集合
//    private List<AnimeDescDetailsBean> animeDescDetailsBeans = new ArrayList<>();
    // 番剧多季集合
    private List<AnimeDescRecommendBean> animeDescMultiBeans = new ArrayList<>();
    // 番剧推荐集合
    private List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
    // 用于imomoe多个播放地址
//    private List<List<AnimeDescDetailsBean>> multipleAnimeDescDetailsBeans;

    // 播放列表集合
    private List<AnimeDramasBean> animeDramasBeans = new ArrayList<>();

    /*public List<AnimeDescDetailsBean> getAnimeDescDetailsBeans() {
        return animeDescDetailsBeans;
    }

    public void setAnimeDescDetailsBeans(List<AnimeDescDetailsBean> animeDescDetailsBeans) {
        this.animeDescDetailsBeans = animeDescDetailsBeans;
    }*/

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

    /*public List<List<AnimeDescDetailsBean>> getMultipleAnimeDescDetailsBeans() {
        return multipleAnimeDescDetailsBeans;
    }

    public void setMultipleAnimeDescDetailsBeans(List<List<AnimeDescDetailsBean>> multipleAnimeDescDetailsBeans) {
        this.multipleAnimeDescDetailsBeans = multipleAnimeDescDetailsBeans;
    }*/
}
