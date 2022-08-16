package my.project.sakuraproject.bean;

import java.util.List;

public class AnimeDramasBean {
    private String listTitle;
    private List<AnimeDescDetailsBean> animeDescDetailsBeanList;

    public String getListTitle() {
        return listTitle;
    }

    public void setListTitle(String listTitle) {
        this.listTitle = listTitle;
    }

    public List<AnimeDescDetailsBean> getAnimeDescDetailsBeanList() {
        return animeDescDetailsBeanList;
    }

    public void setAnimeDescDetailsBeanList(List<AnimeDescDetailsBean> animeDescDetailsBeanList) {
        this.animeDescDetailsBeanList = animeDescDetailsBeanList;
    }
}
