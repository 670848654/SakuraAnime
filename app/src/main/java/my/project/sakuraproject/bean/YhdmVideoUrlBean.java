package my.project.sakuraproject.bean;

import java.io.Serializable;

@Deprecated
public class YhdmVideoUrlBean implements Serializable {
    private boolean isHttp;
    private String vidOrUrl;

    public boolean isHttp() {
        return isHttp;
    }

    public void setHttp(boolean http) {
        isHttp = http;
    }

    public String getVidOrUrl() {
        return vidOrUrl;
    }

    public void setVidOrUrl(String vidOrUrl) {
        this.vidOrUrl = vidOrUrl;
    }
}
