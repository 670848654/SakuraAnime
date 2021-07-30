package my.project.sakuraproject.sniffing;
import android.view.View;

/**
 * 嗅探url过滤器
 */
public interface SniffingFilter {

    String[] DEFAULT_TYPE = {".m3u8",".mp4",".3gp",".wmv",".avi",".rm"};

    /**
     * 用来过滤视频连接，m3u8,mp4等
     * @param webView
     * @param url
     * @return
     */
    SniffingVideo onFilter(View webView, String url);

}