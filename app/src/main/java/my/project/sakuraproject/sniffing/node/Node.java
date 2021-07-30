package my.project.sakuraproject.sniffing.node;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;

/**
 * @author fanchen
 */
public class Node {

    private Element element;

    public Node(String html) {
        this.element = Jsoup.parse(html).body();
    }

    public Node(Element element) {
        this.element = element;
    }

    public Node id(String id) throws Exception {
        Element elementById = element.getElementById(id);
        return elementById != null ? new Node(elementById) : new Node("");
    }

    public boolean isEmpty() {
        return element == null;
    }

    public List<Node> list(String cssQuery) {
        List<Node> list = new LinkedList<>();
        if (element == null)
            return list;
        Elements elements = element.select(cssQuery);
        if (elements == null || elements.size() == 0)
            return list;
        for (Element e : elements) {
            list.add(new Node(e));
        }
        return list;
    }

    public List<Node> listTagClass(String tag, String clazz) {
        List<Node> list = new LinkedList<>();
        if (element == null)
            return list;
        Elements li = element.getElementsByTag(tag);
        for (int i = 0 ; i < li.size() ; i ++){
            Element e = li.get(i);
            String aClass = e.attr("class").trim();
            if(clazz.equals(aClass)){
                list.add(new Node(e));
            }
        }
        return list;
    }

    public int childsSize(String cssQuery) {
        return element == null ? 0 : element.select(cssQuery).size();
    }

    public int size() {
        return element != null ? element.getAllElements().size() : 0;
    }

    public String text() {
        return element != null ? element.text().trim() : "";
    }

    public String text(String cssQuery) {
        if (element == null) return "";
        Elements select = element.select(cssQuery);
        if (select != null && select.first() != null) {
            return select.first().text().trim();
        }
        return "";
    }

    public String textAt(String cssQuery,int position) {
        if (element == null) return "";
        Elements select = element.select(cssQuery);
        if (select != null && select.size() > position) {
            return select.get(position).text().trim();
        }
        return "";
    }

    public String text(String cssQuery, int start, int end) {
        String s = text(cssQuery);
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        if (start < 0) {
            return s;
        }
        if (end < 0) {
            return s.substring(start);
        }
        if (end > s.length()) {
            return s.substring(start);
        }
        return s.substring(start, end);
    }

    public String text(String cssQuery, int start) {
        return text(cssQuery, start, -1);
    }

    public String text(String cssQuery, String regex, int index) {
        String s = text(cssQuery);
        if (TextUtils.isEmpty(s)) {
            return "";
        } else {
            String[] split = s.split(regex);
            if (split.length > index) {
                return split[index];
            } else {
                return "";
            }
        }
    }

    public String attr(String attr) {
        return element == null ? "" : element.attr(attr);
    }

    public String attr(String attr, String regex, int index) {
        String at = attr(attr);
        if (TextUtils.isEmpty(at)) {
            return "";
        } else {
            String[] split = at.split(regex);
            if (split.length > index) {
                return split[index];
            } else {
                return "";
            }
        }
    }

    public String attr(String cssQuery, String attr) {
        if (element == null)
            return "";
        Element first = element.select(cssQuery).first();
        if (first == null)
            return "";
        return first.attr(attr);
    }

    public String attr(String cssQuery, String attr, String regex, int index) {
        String at = attr(cssQuery, attr);
        if (TextUtils.isEmpty(at)) {
            return "";
        } else {
            String[] split = at.split(regex);
            if (split.length > index) {
                return split[index];
            } else {
                return "";
            }
        }
    }

    public Node last(String cssQuery) {
        Element last = element.select(cssQuery).last();
        return last != null ? new Node(last) : new Node("");
    }

    public Node first(String cssQuery) {
        Element first = element.select(cssQuery).first();
        return first != null ? new Node(first) : new Node("");
    }

    public String html() {
        if (element == null) return "";
        return element.html();
    }

    public String html(String cssQuery) {
        if (element == null) return "";
        Elements select = element.select(cssQuery);
        if (select == null) return "";
        return select.html();
    }

    public String textWithSubstring(String cssQuery, int start, int end) {
        return NodeHelper.substring(text(cssQuery), start, end);
    }

    public String textWithSubstring(String cssQuery, int start) {
        return textWithSubstring(cssQuery, start, -1);
    }

    public String textWithSplit(String cssQuery, String regex, int index) {
        return NodeHelper.split(text(cssQuery), regex, index);
    }

    public String src() {
        return attr("src");
    }

    public String src(String cssQuery) {
        return attr(cssQuery, "src");
    }

    public String href() {
        return attr("href");
    }

    public String href(String cssQuery) {
        return attr(cssQuery, "href");
    }

    public String hrefWithSubString(int start, int end) {
        return attrWithSubString("href", start, end);
    }

    public String hrefWithSubString(int start) {
        return hrefWithSubString(start, -1);
    }

    public String hrefWithSubString(String cssQuery, int start, int end) {
        return attrWithSubString(cssQuery, "href", start, end);
    }

    public String hrefWithSubString(String cssQuery, int start) {
        return hrefWithSubString(cssQuery, start, -1);
    }

    public String hrefWithSplit(int index) {
        return splitHref(href(), index);
    }

    public String hrefWithSplit(String cssQuery, int index) {
        return splitHref(href(cssQuery), index);
    }

    private String splitHref(String str, int index) {
        if (str == null) {
            return null;
        }
        str = str.replaceFirst(".*\\..*?/", "");
        str = str.replaceAll("[/\\.=\\?]", " ");
        str = str.trim();
        return NodeHelper.split(str, "\\s+", index);
    }

    public String attrWithSubString(String attr, int start, int end) {
        return NodeHelper.substring(attr(attr), start, end);
    }

    public String attrWithSubString(String attr, int start) {
        return attrWithSubString(attr, start, -1);
    }

    public String attrWithSubString(String cssQuery, String attr, int start, int end) {
        return NodeHelper.substring(attr(cssQuery, attr), start, end);
    }

    public String attrWithSubString(String cssQuery, String attr, int start) {
        return attrWithSubString(cssQuery, attr, start, -1);
    }

    public String attrWithSplit(String attr, String regex, int index) {
        return NodeHelper.split(attr(attr), regex, index);
    }

    public String attrWithSplit(String cssQuery, String attr, String regex, int index) {
        return NodeHelper.split(attr(cssQuery, attr), regex, index);
    }

    public Element getElement() {
        return element;
    }


}