package my.project.sakuraproject.api;

public class Api {
    //检测更新
    public final static String CHECK_UPDATE = "https://api.github.com/repos/670848654/SakuraAnime/releases/latest";
    //樱花动漫解析Api
    @Deprecated
    public final static String PARSE_API = "http://tup.yhdm.so/?vid=%s";

    // SiliSili 搜索
    public final static String SILISILI_SEARCH = "/vodsearch/?wd=%s&page=%s";

    // malimali 首页新番时间表
    public final static String MALIMALI_JAPAN_WEEK = "/index.php/label/week.html";
    // malimali 搜索
    public final static String MALIMALI_SEARCH = "/vodsearch/%s----------%s---.html";
    // malimali 分类
//    https://www.malimali6.com/vodshow/10--hits-%E6%90%9E%E7%AC%91--B---1---2022.html
    public final static String MALIMALI_TAG = "/vodshow/%s--%s-%s--%s---%s---%s.html"; // 1, 排序, 分类, 字母, 分页, 年代
//    public final static String MALIMALI_TAG = "/vodshow/%s-%s-%s-%s-%s-%s.html"; // 1, 排序, 分类, 字母, 分页, 年代
    public final static String MALIMALI_TAG_DEFAULT = "1"; // 默认分类【全部】
    public final static String MALIMALI_CHINA = "9";
    public final static String MALIMALI_JAPAN = "10";
    public final static String MALIMALI_EUROPE = "11";

    // 免费的VIP解析接口用于解析类似： https://v.qq.com/x/cover/ww18u675tfmhas6/v0034fqhcph.html
    public final static String[] PARSE_TITLES = new String[] {"思古解析", "夜幕解析", "虾米解析", "M3U8.TV解析", "纯净/B站解析 [使用WebView播放]"};
    public final static String[] PARSE_INTERFACE = new String[] {
            "https://jsap.attakids.com/?url=%s",
            "https://www.yemu.xyz/?url=%s",
            "https://jx.xmflv.com/?url=%s",
            "https://jx.m3u8.tv/jiexi/?url=%s",
            "https://z1.m1907.cn/?jx=%s"
    };

    // 弹幕接口
    public final static String DANMU_API = "https://api.danmu.oyyds.top/api/message/getSomeV3?keyword=%s&number=%s&type=1&platforms=base,dandan&dandanRelated=true";
}
