package my.project.sakuraproject.bean;

import java.io.Serializable;

public class ImomoeVideoUrlBean implements Serializable {
    private boolean isHttp;
    private String vidOrUrl;
    private String param;

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

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
