package my.project.sakuraproject.api;

public class Api {
    //检测更新
    public final static String CHECK_UPDATE = "https://api.github.com/repos/670848654/SakuraAnime/releases/latest";
    //樱花动漫解析Api
    @Deprecated
    public final static String PARSE_API = "http://tup.yhdm.so/?vid=%s";

    // SiliSili 搜索
    public final static String SILISILI_SEARCH = "/vodsearch/?wd=%s&page=%s";
    // SiliSili 详情目录TAG
    public final static String SILISILI_DESC_TAG_SEARCH = "/vodsearch%spage/%s";
    // SiliSili TAG
    public final static String SLILISILI_XFRM = "xinfanriman";
    public final static String SILISILI_XFGM = "xinfanguoman";
    public final static String SILISILI_WJDM = "dongmanfanju";
    public final static String SILISILI_JCB = "juchang";

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
