package my.project.sakuraproject.bean;

public class Refresh {
    private int index; // 0 刷新首页 1 刷新收藏 2 刷新历史记录 3 刷新下载记录

    public Refresh(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
