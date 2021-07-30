package my.project.sakuraproject.sniffing;

import android.view.View;

public class DefaultFilter implements SniffingFilter {

    @Override
    public SniffingVideo onFilter(View webView, String url) {
        for (String type : DEFAULT_TYPE) {
            if (url.contains(type)) {
                if(url.contains("=")){
                    String[] split = url.split("=");
                    if(split[1].startsWith("http") && split[1].lastIndexOf(type) == split[1].length() - type.length()){
                        url = split[1];
                    }
                }
                Object[] content = Util.getContent(url);
                int len = (int) content[0];
                String ctp = (String) content[1];
                if(ctp.toLowerCase().contains("video") || ctp.toLowerCase().contains("mpegurl")){
                    return new SniffingVideo(url, type, len, ctp);
                }
            }
        }
        return null;
    }

}
