package my.project.sakuraproject.bean;

public class WebviewBean {
    private String title;
    private String url;
    private boolean select;
    private boolean originalPage;
    private boolean originalAddress;

    public WebviewBean(String title, String url, boolean select, boolean originalPage, boolean originalAddress) {
        this.title = title;
        this.url = url;
        this.select = select;
        this.originalPage = originalPage;
        this.originalAddress = originalAddress;
    }

    public boolean isOriginalPage() {
        return originalPage;
    }

    public void setOriginalPage(boolean originalPage) {
        this.originalPage = originalPage;
    }

    public boolean isOriginalAddress() {
        return originalAddress;
    }

    public void setOriginalAddress(boolean originalAddress) {
        this.originalAddress = originalAddress;
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

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
