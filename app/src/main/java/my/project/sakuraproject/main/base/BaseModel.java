package my.project.sakuraproject.main.base;

import java.io.IOException;
import my.project.sakuraproject.util.Utils;
import okhttp3.Response;

public class BaseModel {

    protected boolean isImomoe() {
        return Utils.isImomoe();
    }

    /**
     * 解码方式 yhdm -> utf-8 imomoe -> gb2312
     * @param response
     * @return
     * @throws IOException
     */
    protected String getBody(Response response) throws IOException {
        return new String(response.body().bytes(), isImomoe() ? "gb2312" : "UTF-8");
    }

}
