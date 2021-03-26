package my.project.sakuraproject.bean;

public class Event {
    private boolean isImomoe;
    private int nowSource;
    private int clickIndex;

    public Event() {

    }

    public Event(boolean isImomoe, int nowSource, int clickIndex) {
        this.isImomoe = isImomoe;
        this.nowSource = nowSource;
        this.clickIndex = clickIndex;
    }

    public boolean isImomoe() {
        return isImomoe;
    }

    public void setImomoe(boolean imomoe) {
        isImomoe = imomoe;
    }

    public int getNowSource() {
        return nowSource;
    }

    public void setNowSource(int nowSource) {
        this.nowSource = nowSource;
    }

    public int getClickIndex() {
        return clickIndex;
    }

    public void setClickIndex(int clickIndex) {
        this.clickIndex = clickIndex;
    }
}
