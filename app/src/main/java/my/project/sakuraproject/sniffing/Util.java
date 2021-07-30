package my.project.sakuraproject.sniffing;

import android.text.TextUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import my.project.sakuraproject.sniffing.node.Node;

public class Util {

    public static final String HTMLFLAG = "<SniffingVideo>SniffingVideo</SniffingVideo>";

    public static Object[] getContent(String url) {
        Object[] objects = new Object[2];
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            if (url.startsWith("https")) {
                HttpsURLConnection https = (HttpsURLConnection) urlConnection;
                // 方式一，相信所有
                trustAllHosts(https);
                // 方式二，覆盖默认验证方法
                https.getHostnameVerifier();
                // 方式三，不校验
                https.setHostnameVerifier(DO_NOT_VERIFY);
            }
            urlConnection.setRequestMethod("HEAD");
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 200){
                objects[0] = urlConnection.getContentLength();
                objects[1] = urlConnection.getContentType();
            }
            LogUtil.e("SniffingUtil","getContent code = " + responseCode);
        } catch (Exception e) {
            LogUtil.e("SniffingUtil","getContent error = " + e.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        if (objects[0] == null) objects[0] = -1;
        if (objects[1] == null) objects[1] = "";
        return objects;
    }

    public static String containsType(String url) {
        for (String type : SniffingFilter.DEFAULT_TYPE) {
            if (url.contains(type)) {
                return type;
            }
        }
        return null;
    }

    public static String extracIframe(Node ifran) {
        String aClass = ifran.attr("class");
        String id = ifran.attr("id");
        String name = ifran.attr("name");
        if ("100%".equals(ifran.attr("width")) && "100%".equals(ifran.attr("height"))) {
            return ifran.attr("src");
        }else if ("true".equals(ifran.attr("allowfullscreen"))) {
            return ifran.attr("src");
        }else if (lowerContains(aClass, "player") || lowerContains(id, "player") || lowerContains(name, "player")) {
            return ifran.attr("src");
        } else if (lowerContains(aClass, "video") || lowerContains(id, "video") || lowerContains(name, "video")) {
            return ifran.attr("src");
        } else if (lowerContains(aClass, "m3u") || lowerContains(id, "m3u") || lowerContains(name, "m3u")) {
            return ifran.attr("src");
        }
        return null;
    }

    public static boolean lowerContains(String value, String key) {
        return value.toLowerCase().contains(key);
    }

    /**
     * 對url進行包裝
     *
     * @param url
     * @return
     */
    public static  String warpUrl(String mURL,String url) {
        try {
            if (url.startsWith("//")) {
                url = "http:" + url;
            } else if (url.startsWith("/")) {
                String[] split = mURL.split("/");
                url = split[0] + "//" + split[2] + url;
            } else if (url.startsWith(".") && (mURL.contains("url=") || mURL.contains("v="))) {
                String[] split = mURL.split("=");
                int i = split[0].lastIndexOf("/");
                url = split[0].substring(0, i) + url.substring(1);
            } else if (url.startsWith(".")) {
                int i = mURL.lastIndexOf("/");
                url = mURL.substring(0, i) + url.substring(1);
            }else if(url.startsWith("http")){
                return url;
            }else{
                String[] split = mURL.split("/");
                return split[0] + "//" + split[2] + "/" + url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 執行js 獲取 html
     *
     * @param js
     */
    /*public static void evalScript(WebView view, String js) {
        if (TextUtils.isEmpty(js) || view == null) return;
        String newJs = "javascript:" + js + "(document.getElementsByTagName('html')[0].innerHTML + '" + HTMLFLAG + "');";
        view.loadUrl(newJs);
    }*/

    /**
     * 執行js 獲取 html
     * @param view
     * @param js
     */
    public static void evalScript(android.webkit.WebView view, String js) {
        if (TextUtils.isEmpty(js) || view == null) return;
        String newJs = "javascript:" + js + "(document.getElementsByTagName('html')[0].innerHTML + '" + HTMLFLAG + "');";
        view.loadUrl(newJs);
    }

    /**
     * 覆盖java默认的证书验证
     */
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    }};

    /**
     * 设置不验证主机
     */
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 信任所有
     * @param connection
     * @return
     */
    private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldFactory;
    }


}