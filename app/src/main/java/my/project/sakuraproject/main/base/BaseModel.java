package my.project.sakuraproject.main.base;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import my.project.sakuraproject.R;
import my.project.sakuraproject.util.SharedPreferencesUtils;
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
//        return new String(response.body().bytes(), isImomoe() ? "gb2312" : "UTF-8");
        return new String(response.body().bytes(), "UTF-8");
    }

    /**
     * 对中文编码
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    protected String encodeUrl(String url) throws UnsupportedEncodingException {
        String resultURL = "";
        for (int i = 0; i < url.length(); i++) {
            char charAt = url.charAt(i);
            if (isChineseChar(charAt)) {
                String encode = URLEncoder.encode(charAt+"","GB2312");
                resultURL+=encode;
            }else {
                resultURL+=charAt;
            }
        }
        return resultURL;
    }

    /**
     * 是否是中文
     * @param c
     * @return
     */
    private boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    /***********************************  仅用于详情页面  ***********************************/
    /**
     * 网页编码
     * @param response
     * @return
     * @throws IOException
     */
    protected String getHtmlBody(Response response, boolean isImomoe) throws IOException {
//        return new String(response.body().bytes(), isImomoe ? "gb2312" : "UTF-8");
        return new String(response.body().bytes(), "UTF-8");
    }

    /**
     * 包含view为imomoe源
     * @param isImomoe
     * @return
     */
    public static String getDomain(boolean isImomoe) {
        String domain = isImomoe ?
        (String) SharedPreferencesUtils.getParam(Utils.getContext(), "imomoe_domain", Utils.getString(R.string.imomoe_url))
        :
        (String) SharedPreferencesUtils.getParam(Utils.getContext(), "domain", Utils.getString(R.string.domain_url));
        return domain;
    }
}
