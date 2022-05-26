package my.project.sakuraproject.api;

public class Api {
    //检测更新
    public final static String CHECK_UPDATE = "https://api.github.com/repos/670848654/SakuraAnime/releases/latest";
    //樱花动漫解析Api
    public final static String PARSE_API = "http://tup.yhdm.so/?vid=%s";
    //imomoe视频解析Api
//    public final static String IMOMOE_PARSE_API = "https://saas.jialingmm.net/code.php?type=%s&vid=%s&userlink=%s";
    // malimali 首页新番时间表
    public final static String MALIMALI_JAPAN_WEEK = "/index.php/label/week.html";
    // malimali 搜索
    public final static String MALIMALI_SEARCH = "vodsearch/%s----------%s---.html";
    // malimali 分类
    public final static String MALIMALI_TAG = "vodshow/%s-%s-%s-%s-%s-%s.html"; // 10, 排序, 分类, 字母, 分页, 年代
    public final static String MALIMALI_CHINA = "9";
    public final static String MALIMALI_JAPAN = "10";
    public final static String MALIMALI_EUROPE = "11";
}
