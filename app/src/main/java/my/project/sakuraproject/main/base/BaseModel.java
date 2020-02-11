package my.project.sakuraproject.main.base;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BaseModel {
    public static boolean hasRefresh(Document body) {
        Element meta = body.select("meta[http-equiv=refresh]").first();
        return meta != null;
    }
}
