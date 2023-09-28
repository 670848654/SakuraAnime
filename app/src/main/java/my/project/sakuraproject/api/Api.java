package my.project.sakuraproject.api;

public class Api {
    //检测更新
    public final static String CHECK_UPDATE = "https://api.github.com/repos/670848654/SakuraAnime/releases/latest";
    /**
     * 2023年9月27日14:49:27 发现YHDM需要加前缀
     */
    public final static String YHDM_PLAY_PATH = "https://tup.iyinghua.com/?vid=%s";
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
    public final static String SILISILI_RANK = "/map.html";

    // 免费的VIP解析接口用于解析类似： https://v.qq.com/x/cover/ww18u675tfmhas6/v0034fqhcph.html
    public final static String[] PARSE_TITLES = new String[] {"思古解析", "夜幕解析", "虾米解析", "M3U8.TV解析", "纯净/B站解析 [使用WebView播放]"};
    public final static String[] PARSE_INTERFACE = new String[] {
            "https://jsap.attakids.com/?url=%s",
            "https://www.yemu.xyz/?url=%s",
            "https://jx.xmflv.com/?url=%s",
            "https://jx.m3u8.tv/jiexi/?url=%s",
            "https://z1.m1907.cn/?jx=%s"
    };

    /**
     * @deprecated 弹幕接口 [my_danmu_pub]不公开了~ {@link Api#SILISILI_PARSE_API}
     */
    @Deprecated
    public final static String DANMU_API = "https://api.danmu.oyyds.top/api/message/getSomeV3?keyword=%s&number=%s&type=1&platforms=base,dandan&dandanRelated=true";

    /**
     * 嘶哩嘶哩站点弹幕接口
     * @param {站点地址} {番剧名称} {集数列表下标}
     */
    public final static String SILISILI_DANMU_API = "%s/static/player/AB/api.php?act=dm&m=get&id=%s%s";

    /**
     * 嘶哩嘶哩站点需解析的播放路劲解析地址
     * @param {站点地址} {播放地址}
     */
    public final static String SILISILI_PARSE_API = "%s/static/player/Aliplayer/iindex.php?url=%s";
}
