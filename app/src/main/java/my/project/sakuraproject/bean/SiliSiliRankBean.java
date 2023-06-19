package my.project.sakuraproject.bean;

import java.util.List;

/**
 * silisili排行榜实体类
 */
public class SiliSiliRankBean {
    private String title;
    private List<RankItem> rankItems;

    public SiliSiliRankBean(String title, List<RankItem> rankItems) {
        this.title = title;
        this.rankItems = rankItems;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<RankItem> getRankItems() {
        return rankItems;
    }

    public void setRankItems(List<RankItem> rankItems) {
        this.rankItems = rankItems;
    }

    public static class RankItem {
        private String index;
        private String title;
        private String url;
        private String hot;
        private String score;

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHot() {
            return hot;
        }

        public void setHot(String hot) {
            this.hot = hot;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }
    }
}
