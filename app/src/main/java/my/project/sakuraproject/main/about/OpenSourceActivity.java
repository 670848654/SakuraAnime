package my.project.sakuraproject.main.about;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.SourceAdapter;
import my.project.sakuraproject.bean.SourceBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
import my.project.sakuraproject.util.Utils;

public class OpenSourceActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private SourceAdapter adapter;
    private List<SourceBean> list = new ArrayList<>();

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_source;
    }

    @Override
    protected void init() {
        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initSwipe();
        initList();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.os_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> supportFinishAfterTransition());
    }

    public void initSwipe() {
        mSwipe.setEnabled(false);
    }

    public void initList() {
        list.add(new SourceBean("jsoup", "jhy", "jsoup: Java HTML Parser, with best of DOM, CSS, and jquery", "https://github.com/jhy/jsoup"));
        list.add(new SourceBean("BaseRecyclerView\nAdapterHelper", "CymChad", "BRVAH:Powerful and flexible RecyclerAdapter", "https://github.com/CymChad/BaseRecyclerViewAdapterHelper"));
        list.add(new SourceBean("Glide", "bumptech", "An image loading and caching library for Android focused on smooth scrolling", "https://github.com/bumptech/glide"));
        list.add(new SourceBean("glide-transformations", "wasabeef", "An Android transformation library providing a variety of image transformations for Glide.", "https://github.com/wasabeef/glide-transformations"));
        list.add(new SourceBean("EasyPermissions", "googlesamples", "Simplify Android M system permissions", "https://github.com/googlesamples/easypermissions"));
        list.add(new SourceBean("MaterialEditText", "rengwuxian", "EditText in Material Design", "https://github.com/rengwuxian/MaterialEditText"));
        list.add(new SourceBean("JZVideo", "Jzvd", "高度自定义的安卓视频框架 MediaPlayer exoplayer ijkplayer ffmpeg", "https://github.com/Jzvd/JZVideo"));
        list.add(new SourceBean("ExoPlayer", "google", "An extensible media player for Android", "https://github.com/google/ExoPlayer"));
        list.add(new SourceBean("Slidr", "r0adkll", "Easily add slide to dismiss functionality to an Activity", "https://github.com/r0adkll/Slidr"));
        list.add(new SourceBean("butterknife", "JakeWharton", "Bind Android views and callbacks to fields and methods.", "https://github.com/JakeWharton/butterknife"));
        list.add(new SourceBean("okhttp", "square", "An HTTP+HTTP/2 client for Android and Java applications.", "https://github.com/square/okhttp"));
//        list.add(new SourceBean("customtabs", "GoogleChrome", "mirrored from https://chromium.googlesource.com/custom-tabs-client", "https://github.com/GoogleChrome/custom-tabs-client"));
        list.add(new SourceBean("Toasty","GrenderG","The usual Toast, but with steroids","https://github.com/GrenderG/Toasty"));
        list.add(new SourceBean("Sniffing","fanchen001","【次元番】使用的，一个基于webview/x5webview的视频嗅探工具,能准确解析绝大多数手机在线视频网站的视频真实链接。","https://github.com/fanchen001/Sniffing"));
        list.add(new SourceBean("AndroidTagView","whilu","A TagView library for Android. Customize your own & Drag effect.","https://github.com/whilu/AndroidTagView"));
        list.add(new SourceBean("AndroidUPnPDemo","zaneCC","android 投屏","https://github.com/zaneCC/AndroidUPnPDemo"));
        list.add(new SourceBean("cling","4thline","UPnP/DLNA library for Java and Android","https://github.com/4thline/cling"));
        list.add(new SourceBean("RippleAnimation", "wuyr", "仿酷安客户端的主题切换动画效果", "https://github.com/wuyr/RippleAnimation"));
    }

    public void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SourceAdapter(list);
        adapter.openLoadAnimation();
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (Utils.isFastClick()) Utils.viewInChrome(this, list.get(position).getUrl());
        });
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this) + 15);
        recyclerView.setAdapter(adapter);
    }
}
