package my.project.sakuraproject.config;

import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.bean.SourceBean;

public enum OpenSourceEnum {
    JSOUP("jsoup", "jhy", "jsoup: Java HTML Parser, with best of DOM, CSS, and jquery", "https://github.com/jhy/jsoup"),
    BASE_RECYCLER_VIEW("BaseRecyclerView\nAdapterHelper", "CymChad", "BRVAH:Powerful and flexible RecyclerAdapter", "https://github.com/CymChad/BaseRecyclerViewAdapterHelper"),
    GLIDE("Glide", "bumptech", "An image loading and caching library for Android focused on smooth scrolling", "https://github.com/bumptech/glide"),
    GLIDE_TRANSFORMATIONS("glide-transformations", "wasabeef", "An Android transformation library providing a variety of image transformations for Glide.", "https://github.com/wasabeef/glide-transformations"),
    PERMISSION_X("PermissionX", "guolindev", "An open source Android library that makes handling runtime permissions extremely easy.", "https://github.com/guolindev/PermissionX"),
    JZ_VIDEO("JZVideo", "Jzvd", "高度自定义的安卓视频框架 MediaPlayer exoplayer ijkplayer ffmpeg", "https://github.com/Jzvd/JZVideo"),
    EXO_PLAYER("ExoPlayer", "google", "An extensible media player for Android", "https://github.com/google/ExoPlayer"),
    IJK_PLAYER("Ijkplayer", "bilibili", "Android/iOS video player based on FFmpeg n3.4, with MediaCodec, VideoToolbox support.", "https://github.com/bilibili/ijkplayer"),
    BUTTERKNIFE("butterknife", "JakeWharton", "Bind Android views and callbacks to fields and methods.", "https://github.com/JakeWharton/butterknife"),
    OKHTTP("okhttp", "square", "An HTTP+HTTP/2 client for Android and Java applications.", "https://github.com/square/okhttp"),
    SNIFFING("Sniffing", "fanchen001", "【次元番】使用的，一个基于webview/x5webview的视频嗅探工具,能准确解析绝大多数手机在线视频网站的视频真实链接。", "https://github.com/fanchen001/Sniffing"),
    ANDROID_UPNP_DEMO("AndroidUPnPDemo", "zaneCC", "android 投屏", "https://github.com/zaneCC/AndroidUPnPDemo"),
    CLING("cling", "4thline", "UPnP/DLNA library for Java and Android", "https://github.com/4thline/cling"),
    RIPPLE_ANIMATION("RippleAnimation", "wuyr", "仿酷安客户端的主题切换动画效果", "https://github.com/wuyr/RippleAnimation"),
    EXPANDABLE_TEXT_VIEW("ExpandableTextView", "MZCretin", "实现类似微博内容，@用户，链接高亮，@用户和链接可点击跳转，可展开和收回的TextView", "https://github.com/MZCretin/ExpandableTextView"),
    FASTJSON("fastjson", "alibaba", "A fast JSON parser/generator for Java.", "https://github.com/alibaba/fastjson"),
    EVENTBUS("EventBus", "greenrobot", "Event bus for Android and Java that simplifies communication between Activities, Fragments, Threads, Services, etc. Less code, better quality.", "https://github.com/greenrobot/EventBus"),
    ARIA("Aria", "AriaLyy", "下载可以很简单\naria.laoyuyu.me/aria_doc/", "https://github.com/AriaLyy/Aria"),
    NANOHTTPD("nanohttpd", "NanoHttpd", "Tiny, easily embeddable HTTP server in Java.", "https://github.com/NanoHttpd/nanohttpd"),
    DANMAKU_FLAME_MASTER("DanmakuFlameMaster", "bilibili", "Android开源弹幕引擎·烈焰弹幕使 ～", "https://github.com/bilibili/DanmakuFlameMaster"),
    BANNER("banner", "youth5201314", "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25Banner 2.0 来了！Android广告图片轮播控件，内部基于ViewPager2实现，Indicator和UI都可以自定义。", "https://github.com/youth5201314/banner");
    private String title;
    private String author;
    private String introduction;
    private String url;

    OpenSourceEnum(String title, String author, String introduction, String url) {
        this.title = title;
        this.author = author;
        this.introduction = introduction;
        this.url = url;
    }

    public static List<SourceBean> getSourceList() {
        List<SourceBean> sourceBeans = new ArrayList<>();
        for (OpenSourceEnum openSourceEnum : OpenSourceEnum.values()) {
            sourceBeans.add(new SourceBean(openSourceEnum.getTitle(), openSourceEnum.getAuthor(), openSourceEnum.getIntroduction(), openSourceEnum.getUrl()));
        }
        return sourceBeans;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getUrl() {
        return url;
    }
}
