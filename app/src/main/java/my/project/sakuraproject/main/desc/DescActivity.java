package my.project.sakuraproject.main.desc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.core.processor.IVodTsUrlConverter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ctetin.expandabletextviewlibrary.ExpandableTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.OnClick;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.AnimeDescDetailsAdapter;
import my.project.sakuraproject.adapter.AnimeDescDramaAdapter;
import my.project.sakuraproject.adapter.AnimeDescRecommendAdapter;
import my.project.sakuraproject.adapter.DownloadDramaAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.DownloadDramaBean;
import my.project.sakuraproject.bean.Event;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.YhdmVideoUrlBean;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.custom.MyTextView;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.search.SearchActivity;
import my.project.sakuraproject.main.tag.MaliTagActivity;
import my.project.sakuraproject.main.video.DownloadVideoContract;
import my.project.sakuraproject.main.video.DownloadVideoPresenter;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.services.DownloadService;
import my.project.sakuraproject.sniffing.SniffingUICallback;
import my.project.sakuraproject.sniffing.SniffingVideo;
import my.project.sakuraproject.sniffing.web.SniffingUtil;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class DescActivity extends BaseActivity<DescContract.View, DescPresenter> implements DescContract.View, VideoContract.View,
        DownloadVideoContract.View, SniffingUICallback {
    @BindView(R.id.anime_img)
    ImageView animeImg;
    @BindView(R.id.desc)
    ExpandableTextView desc;
    @BindView(R.id.title)
    MyTextView title;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private String sakuraUrl, dramaUrl;
    private String animeTitle;
    private String witchTitle;
    private AlertDialog alertDialog;
    private boolean isFavorite;
    private VideoPresenter videoPresenter;
    private AnimeListBean animeListBean = new AnimeListBean();
    private List<String> animeUrlList = new ArrayList();
    @BindView(R.id.details_list)
    RecyclerView detailsRv;
    private LinearLayoutManager detailsRvLinearLayoutManager;
    @BindView(R.id.multi_list)
    RecyclerView multiRv;
    private LinearLayoutManager multiRvLinearLayoutManager;
    @BindView(R.id.recommend_list)
    RecyclerView recommendRv;
    private LinearLayoutManager recommendRvLinearLayoutManager;
    @BindView(R.id.error_bg)
    RelativeLayout errorBg;
    @BindView(R.id.open_drama)
    RelativeLayout openDrama;
    @BindView(R.id.play_layout)
    LinearLayout playLinearLayout;
    @BindView(R.id.multi_layout)
    LinearLayout multiLinearLayout;
    @BindView(R.id.recommend_layout)
    LinearLayout recommendLinearLayout;
    @BindView(R.id.error_msg)
    TextView error_msg;
    @BindView(R.id.msg)
    CoordinatorLayout msg;
    private RecyclerView lineRecyclerView;
    private AnimeDescDetailsAdapter animeDescDetailsAdapter;
    private AnimeDescRecommendAdapter animeDescRecommendAdapter;
    private AnimeDescRecommendAdapter animeDescMultiAdapter;
    private AnimeDescListBean animeDescListBean = new AnimeDescListBean();
    private ImageView closeDrama;
    private BottomSheetDialog mBottomSheetDialog;
    private AnimeDescDramaAdapter animeDescDramaAdapter;
    @BindView(R.id.desc_view)
    LinearLayout desc_view;
    @BindView(R.id.bg)
    ImageView bg;
    @BindView(R.id.favorite)
    MaterialButton favorite;
    @BindView(R.id.tag_view)
    TagContainerLayout tagContainerLayout;
    @BindView(R.id.update_time)
    MyTextView update_time;
    @BindView(R.id.score_view)
    AppCompatTextView score_view;
    @BindView(R.id.scrollview)
    NestedScrollView scrollView;
    private boolean isImomoe;
    private int clickIndex; // 当前点击剧集
    // 番剧ID
    private String animeId;
    @BindView(R.id.download)
    FloatingActionButton downloadView;
    private SlidrInterface slidrInterface;
    // 下载
    private String savePath; // 下载保存路劲
    private int source; // 番剧源 0 yhdm 1 imomoe
    private DownloadVideoPresenter downloadVideoPresenter; // 下载适配器
    private JSONObject jsonObject; // Aria任务扩展数据
    private RecyclerView downloadRecyclerView;
    private BottomSheetDialog downloadBottomSheetDialog;
    private List<DownloadDramaBean> downloadBean = new ArrayList<>();
    private DownloadDramaAdapter downloadAdapter;
    private ExtendedFloatingActionButton downloadBtn;
    List<String> urls; // 保存
    List<String> dramaNames;
    private M3U8VodOption m3U8VodOption; // 下载m3u8配置

    @Override
    protected DescPresenter createPresenter() {
        return new DescPresenter(sakuraUrl, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_desc;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        StatusBarUtil.setColorForSwipeBack(this, getResources().getColor(R.color.colorPrimaryDark), 0);
        if (isDarkTheme) bg.setVisibility(View.GONE);
        slidrInterface = Slidr.attach(this, Utils.defaultInit());
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) msg.getLayoutParams();
        params.setMargins(10, 0, 10, 0);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> mSwipe.setEnabled(scrollView.getScrollY() == 0));
        desc.setNeedExpend(true);
        getBundle();
        initFab();
        initSwipe();
        initAdapter();
        initTagClick();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            sakuraUrl = bundle.getString("url");
            animeTitle = bundle.getString("name");
            animeUrlList.add(sakuraUrl);
        }
    }

    public void initFab() {
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) downloadView.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 15);
            downloadView.setLayoutParams(params);
        }
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setProgressViewOffset(true, -0, 150);
        mSwipe.setOnRefreshListener(() ->  {
            mSwipe.setRefreshing(true);
            mPresenter.loadData(true);
        });
    }

    @SuppressLint("RestrictedApi")
    public void initAdapter() {
        animeDescDetailsAdapter = new AnimeDescDetailsAdapter(this, animeDescListBean.getAnimeDescDetailsBeans());
        animeDescDetailsAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            playVideo(adapter, position, detailsRv);
        });
        detailsRvLinearLayoutManager = getLinearLayoutManager();
        detailsRv.setLayoutManager(detailsRvLinearLayoutManager);
        detailsRv.setAdapter(animeDescDetailsAdapter);
        detailsRv.setNestedScrollingEnabled(false);
        setEnableSliding(detailsRv, detailsRvLinearLayoutManager);

        animeDescMultiAdapter = new AnimeDescRecommendAdapter(this, animeDescListBean.getAnimeDescMultiBeans());
        animeDescMultiAdapter.setOnItemClickListener((adapter, view, position) -> {
            AnimeDescRecommendBean bean = (AnimeDescRecommendBean) adapter.getItem(position);
            animeTitle = bean.getTitle();
            sakuraUrl = bean.getUrl();
            animeUrlList.add(sakuraUrl);
            openAnimeDesc();
        });
        multiRvLinearLayoutManager = getLinearLayoutManager();
        multiRv.setLayoutManager(multiRvLinearLayoutManager);
        multiRv.setAdapter(animeDescMultiAdapter);
        multiRv.setNestedScrollingEnabled(false);
        setEnableSliding(multiRv, multiRvLinearLayoutManager);

        animeDescRecommendAdapter = new AnimeDescRecommendAdapter(this, animeDescListBean.getAnimeDescRecommendBeans());
        animeDescRecommendAdapter.setOnItemClickListener((adapter, view, position) -> {
            AnimeDescRecommendBean bean = (AnimeDescRecommendBean) adapter.getItem(position);
            animeTitle = bean.getTitle();
            sakuraUrl = bean.getUrl();
            animeUrlList.add(sakuraUrl);
            openAnimeDesc();
        });
        recommendRvLinearLayoutManager = getLinearLayoutManager();
        recommendRv.setLayoutManager(recommendRvLinearLayoutManager);
        if (Utils.checkHasNavigationBar(this)) recommendRv.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        recommendRv.setAdapter(animeDescRecommendAdapter);
        recommendRv.setNestedScrollingEnabled(false);
        setEnableSliding(recommendRv, recommendRvLinearLayoutManager);

        View dramaView = LayoutInflater.from(this).inflate(R.layout.dialog_drama, null);
        lineRecyclerView = dramaView.findViewById(R.id.drama_list);

        animeDescDramaAdapter = new AnimeDescDramaAdapter(this, new ArrayList<>());
        animeDescDramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            mBottomSheetDialog.dismiss();
            playVideo(adapter, position, lineRecyclerView);
        });
        lineRecyclerView.setAdapter(animeDescDramaAdapter);
        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(dramaView);
        closeDrama = dramaView.findViewById(R.id.close_drama);
        closeDrama.setOnClickListener(v-> mBottomSheetDialog.dismiss());

        View downloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_drama, null);
        ExpandableTextView expandableTextView = downloadView.findViewById(R.id.info);
        expandableTextView.setContent(Utils.getString(R.string.download_info));
        expandableTextView.setNeedExpend(true);
        downloadRecyclerView = downloadView.findViewById(R.id.download_list);

        downloadAdapter = new DownloadDramaAdapter(this, new ArrayList<>());
        downloadAdapter.setOnItemClickListener((adapter, view, position) -> {
            DownloadDramaBean bean = (DownloadDramaBean) adapter.getItem(position);
            if (bean.isHasDownload()) {
                CustomToast.showToast(this, "已存在下载任务，请勿重复执行！", CustomToast.WARNING);
                return;
            }
            if (bean.isShouldParse()) {
                // 需要解析才能下载
                downloadSingleParse(position);
                return;
            }
            MaterialButton materialButton = (MaterialButton) adapter.getViewByPosition(downloadRecyclerView, position, R.id.tag_group);
            if (bean.isSelected()) {
                bean.setSelected(false);
                materialButton.setTextColor(getResources().getColor(R.color.text_color_primary));
            } else if (!bean.isSelected()) {
                bean.setSelected(true);
                materialButton.setTextColor(getResources().getColor(R.color.tabSelectedTextColor));
            }
        });
        downloadRecyclerView.setAdapter(downloadAdapter);
        downloadBottomSheetDialog = new BottomSheetDialog(this);
        downloadBottomSheetDialog.setContentView(downloadView);
        downloadBtn = downloadView.findViewById(R.id.download);
        downloadBtn.post(() -> {
            int height = downloadBtn.getHeight();
            downloadRecyclerView.setPadding(0, 0, 0, height + height/2);
        });
        downloadBtn.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            List<DownloadDramaBean> beans = downloadAdapter.getData();
            urls = new ArrayList<>();
            dramaNames = new ArrayList<>();
            for (int i=0,size=beans.size(); i<size; i++) {
                if (beans.get(i).isSelected()) {
                    urls.add(beans.get(i).getUrl());
                    dramaNames.add(beans.get(i).getTitle());
                }
            }
            if (urls.size() == 0) {
                CustomToast.showToast(this, "请至少选择一集", CustomToast.WARNING);
            } else {
                downloadSelected();
                downloadBottomSheetDialog.dismiss();
            }
        });
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        return linearLayoutManager;
    }

    private void setEnableSliding(RecyclerView recommendRv, LinearLayoutManager linearLayoutManager) {
        if (Utils.getSlidrConfig()) return;
        recommendRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (linearLayoutManager.findFirstVisibleItemPosition() > 0)
                    slidrInterface.lock();
                else
                    slidrInterface.unlock();
            }
        });
    }

    private void initTagClick() {
        tagContainerLayout.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                Bundle bundle = new Bundle();
                bundle.putString("title", animeListBean.getTagTitles().get(position));
                bundle.putString("url", animeListBean.getTagUrls().get(position));
                bundle.putBoolean("isMovie", animeListBean.getTagUrls().get(position).contains("movie") ? true : false);
                isImomoe = sakuraUrl.contains("/voddetail/");
                bundle.putBoolean("isImomoe", isImomoe);
                if (!isImomoe)
                    startActivity(new Intent(DescActivity.this, AnimeListActivity.class).putExtras(bundle));
                else {
                    if (sakuraUrl.contains("/voddetail/") && animeListBean.getTagUrls().get(position).contains("-"))
                        startActivity(new Intent(DescActivity.this, SearchActivity.class).putExtras(bundle));
                    else {
                        Bundle tagBundle = new Bundle();
                        tagBundle.putString("title", animeListBean.getTagTitles().get(position));
                        switch (animeListBean.getTagTitles().get(position)) {
                            case MaliTagActivity.FL_JAPAN:
                                tagBundle.putString("homeParam", Api.MALIMALI_JAPAN);
                                break;
                            case MaliTagActivity.FL_CHINA:
                                tagBundle.putString("homeParam", Api.MALIMALI_CHINA);
                                break;
                            case MaliTagActivity.FL_EUROPE:
                                tagBundle.putString("homeParam", Api.MALIMALI_EUROPE);
                                break;
                        }
                        startActivity(new Intent(DescActivity.this, MaliTagActivity.class).putExtras(tagBundle));
                    }
                }
            }

            @Override
            public void onTagLongClick(int position, String text) {

            }

            @Override
            public void onSelectedTagDrag(int position, String text) {

            }

            @Override
            public void onTagCrossClick(int position) {

            }
        });
    }

    @OnClick(R.id.open_drama)
    public void dramaClick() {
        if (!mBottomSheetDialog.isShowing()) {
            mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            mBottomSheetDialog.show();
        }
    }

    @OnClick({R.id.browser})
    public void openBrowser() {
        Utils.viewInChrome(this, BaseModel.getDomain(isImomoe) + sakuraUrl);
    }

    @SuppressLint("RestrictedApi")
    public void openAnimeDesc() {
        downloadView.setVisibility(View.GONE);
        animeImg.setImageDrawable(getDrawable(isDarkTheme ? R.drawable.loading_night : R.drawable.loading_light));
        hideView(tagContainerLayout);
        tagContainerLayout.setTags("");
        hideView(score_view);
        setTextviewEmpty(desc);
        animeDescListBean = new AnimeDescListBean();
        hideView(favorite);
        bg.setImageDrawable(getResources().getDrawable(R.drawable.default_bg));
        mPresenter = new DescPresenter(sakuraUrl, this);
        mPresenter.loadData(true);
    }

    private void setTextviewEmpty(AppCompatTextView appCompatTextView) {
        appCompatTextView.setText("");
    }

    public void playVideo(BaseQuickAdapter adapter, int position, RecyclerView recyclerView) {
        alertDialog = Utils.getProDialog(DescActivity.this, R.string.parsing);
        MaterialButton materialButton = (MaterialButton) adapter.getViewByPosition(recyclerView, position, R.id.tag_group);
        materialButton.setTextColor(getResources().getColor(R.color.tabSelectedTextColor));
        clickIndex = position;
        int playSource = 0;
        String playNumber;
        animeDescListBean.getAnimeDescDetailsBeans().get(position).setSelected(true);
        dramaUrl = animeDescListBean.getAnimeDescDetailsBeans().get(position).getUrl();
        playNumber = animeDescListBean.getAnimeDescDetailsBeans().get(position).getTitle();
        witchTitle = animeTitle + " - " + playNumber;
        animeDescDetailsAdapter.notifyDataSetChanged();
        animeDescDramaAdapter.notifyDataSetChanged();
        videoPresenter = new VideoPresenter(animeTitle, dramaUrl, playSource, playNumber, DescActivity.this);
        videoPresenter.loadData(true);
    }

    /**
     * 播放视频
     *
     * @param animeUrl
     */
    private void playAnime(String animeUrl) {
        cancelDialog();
        switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
            case 0:
                //调用播放器
                VideoUtils.openPlayer(true, this, witchTitle, animeUrl, animeTitle, dramaUrl, animeDescListBean.getAnimeDescDetailsBeans(), clickIndex, animeId, isImomoe);
                break;
            case 1:
                Utils.selectVideoPlayer(this, animeUrl);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (animeUrlList.size() == 1) super.onBackPressed();
        else {
            if (!mSwipe.isRefreshing()) {
                animeUrlList.remove(animeUrlList.size() - 1);
                sakuraUrl = animeUrlList.get(animeUrlList.size() - 1);
                openAnimeDesc();
            } else
                CustomToast.showToast(this, Utils.getString(R.string.load_desc_info), CustomToast.WARNING);
        }
    }

    public void favoriteAnime() {
        isFavorite = DatabaseUtil.favorite(animeListBean, animeId);
        favorite.setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_favorite_white_48dp : R.drawable.baseline_favorite_border_white_48dp));
        favorite.setText(isFavorite ? Utils.getString(R.string.has_favorite) : Utils.getString(R.string.favorite));
        application.showSnackbarMsg(msg, isFavorite ? Utils.getString(R.string.join_ok) : Utils.getString(R.string.join_error));
        EventBus.getDefault().post(new Refresh(1));
    }

    public void setCollapsingToolbar() {
        GlideUrl imgUrl;
        if (isImomoe)
            imgUrl = new GlideUrl(Utils.getImgUrl(animeListBean.getImg(), true));
        else
            imgUrl = new GlideUrl(Utils.getImgUrl(animeListBean.getImg(), false), new LazyHeaders.Builder()
                    .addHeader("Referer", BaseModel.getDomain(false) + "/")
                    .build());
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(300).setCrossFadeEnabled(true).build();
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(isDarkTheme ? R.drawable.loading_night : R.drawable.loading_light)
                .error(R.drawable.error);
        Glide.with(this)
                .load(imgUrl)
                .transition(DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory))
                .apply(options)
                .apply(RequestOptions.bitmapTransform( new BlurTransformation(15, 5)))
                .into(bg);
        Utils.setDefaultImage(this, animeListBean.getImg(), animeListBean.getUrl(), animeImg, false, null, null);
        title.setText(animeListBean.getTitle());
        if (animeListBean.getTagTitles() != null) {
            tagContainerLayout.setTags(animeListBean.getTagTitles());
            showView(tagContainerLayout);
        }else
            hideView(tagContainerLayout);
        if (animeListBean.getDesc().isEmpty())
            hideView(desc);
        else {
            desc.setContent(animeListBean.getDesc());
            showView(desc);
        }
        update_time.setText(animeListBean.getUpdateTime());
        if (!isImomoe) {
            score_view.setText(animeListBean.getScore()+"分");
            showView(score_view);
        }
    }

    /*@OnClick(R.id.spinner)
    public void showMenu() {
        showPopupMenu();
    }*/

    @OnClick(R.id.favorite)
    public void setFavorite() {
        favoriteAnime();
    }

    @OnClick(R.id.exit)
    public void exit() {
        finish();
    }

    @Override
    public void showLoadingView() {
        showEmptyVIew();
        detailsRv.scrollToPosition(0);
        multiRv.scrollToPosition(0);
        recommendRv.scrollToPosition(0);
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                downloadView.setVisibility(View.GONE);
                mSwipe.setRefreshing(false);
                hideView(desc_view);
                hideView(playLinearLayout);
                hideView(multiLinearLayout);
                hideView(recommendLinearLayout);
                error_msg.setText(msg);
                showView(errorBg);
            }
        });
    }

    private void hideView(View view) {
        Utils.fadeOut(view);
        view.setVisibility(View.GONE);
    }

    private void showView(View view) {
        Utils.fadeIn(view);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyVIew() {
        mSwipe.setRefreshing(true);
        downloadView.setVisibility(View.GONE);
        hideView(favorite);
        hideView(desc_view);
        hideView(playLinearLayout);
        hideView(multiLinearLayout);
        hideView(recommendLinearLayout);
        hideView(errorBg);
    }

    @Override
    public void showLog(String url) {
//        runOnUiThread(() -> application.showToastShortMsg(url));
    }

    @Override
    public void showSuccessMainView(AnimeDescListBean bean) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (!isImomoe)
                    downloadView.setVisibility(View.VISIBLE);
                setCollapsingToolbar();
                mSwipe.setRefreshing(false);
                if (isFavorite) DatabaseUtil.updateFavorite(animeListBean, animeId);
                animeDescListBean = bean;
                downloadBean = new ArrayList<>();
                animeDescDetailsAdapter.setNewData(animeDescListBean.getAnimeDescDetailsBeans());
                for (AnimeDescDetailsBean b : animeDescListBean.getAnimeDescDetailsBeans()) {
                    DownloadDramaBean downloadDramaBean = new DownloadDramaBean();
                    downloadDramaBean.setTitle(b.getTitle());
                    downloadDramaBean.setSelected(false);
                    downloadDramaBean.setUrl(b.getUrl());
                    downloadBean.add(downloadDramaBean);
                }
                if (bean.getAnimeDescMultiBeans().size() > 0)
                    showView(multiLinearLayout);
                else
                    hideView(multiLinearLayout);
                animeDescMultiAdapter.setNewData(bean.getAnimeDescMultiBeans());
                animeDescRecommendAdapter.setNewData(bean.getAnimeDescRecommendBeans());
                setAnimeDescDramaAdapter(0);
                showView(desc_view);
                showView(playLinearLayout);
                showView(recommendLinearLayout);
            }
        });
    }

    private void setAnimeDescDramaAdapter(int sourceIndex) {
        if (animeDescListBean.getAnimeDescDetailsBeans().size() > 4)
            showView(openDrama);
        else
            hideView(openDrama);
        animeDescDramaAdapter.setNewData(animeDescListBean.getAnimeDescDetailsBeans());
        downloadAdapter.setNewData(downloadBean);
    }

    @Override
    public void showSuccessDescView(AnimeListBean bean) {
        animeListBean = bean;
        animeTitle = animeListBean.getTitle();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void showSuccessFavorite(boolean is) {
        isFavorite = is;
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (!favorite.isShown()) {
                    favorite.setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_favorite_white_48dp : R.drawable.baseline_favorite_border_white_48dp));
                    favorite.setText(isFavorite ? Utils.getString(R.string.has_favorite) : Utils.getString(R.string.favorite));
                    showView(favorite);
                }
            }
        });
    }

    @Override
    public void showEmptyDram(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                setCollapsingToolbar();
                showView(desc_view);
                hideView(playLinearLayout);
                hideView(multiLinearLayout);
                hideView(recommendLinearLayout);
                error_msg.setText(msg);
                showView(errorBg);
            }
        });
    }

    @Override
    public void isImomoe(boolean isImomoe) {
        source = isImomoe ? 1 : 0;
        this.isImomoe = isImomoe;
    }

    @Override
    public void getAnimeId(String animeId) {
        this.animeId = animeId;
    }

    @Override
    public void cancelDialog() {
        Utils.cancelDialog(alertDialog);
    }

    @Override
    public void showYhdmVideoSuccessView(List<String> list) {
        runOnUiThread(() -> {
            if (list.size() == 1)
                playAnime(list.get(0));
            else
                VideoUtils.showMultipleVideoSources(this,
                        list,
                        (dialog, index) -> playAnime(list.get(index)),
                        (dialog, which) -> {
                            cancelDialog();
                            dialog.dismiss();
                        }, 1, false);
        });
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> {
            CustomToast.showToast(this, Utils.getString(R.string.open_web_view), CustomToast.WARNING);
            VideoUtils.openDefaultWebview(this, dramaUrl.contains("/voddetail/") ? BaseModel.getDomain(true) + dramaUrl : BaseModel.getDomain(false) + dramaUrl);
        });
    }

    @Override
    public void getVideoError() {
        runOnUiThread(() -> {
            CustomToast.showToast(this, Utils.getString(R.string.error_700), CustomToast.ERROR);
        });
    }

    @Override
    public void showSuccessYhdmDramasView(List<AnimeDescDetailsBean> list) {

    }

    @Override
    public void errorDramaView() {

    }

    @Override
    public void showSuccessImomoeVideoUrlView(String playUrl) {
        runOnUiThread(() -> {
            playAnime(playUrl);
        });
    }

    @Override
    public void showSuccessImomoeDramasView(List<AnimeDescDetailsBean> bean) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != videoPresenter)
            videoPresenter.detachView();
        if (null != downloadVideoPresenter)
            downloadVideoPresenter.detachView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        clickIndex = event.getClickIndex();
        animeDescListBean.getAnimeDescDetailsBeans().get(clickIndex).setSelected(true);
        animeDescDetailsAdapter.notifyDataSetChanged();
        animeDescDramaAdapter.notifyDataSetChanged();
    }

    /****************************************** 下载相关 ******************************************/
    /**
     * 显示downloadBottomSheetDialog
     */
    @OnClick(R.id.download)
    public void download() {
        checkHasDownload();
        downloadBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        downloadBottomSheetDialog.show();
    }

    /**
     * 下载配置
     */
    private void createDownloadConfig() {
        if (isImomoe)
            savePath = Environment.getExternalStorageDirectory() + "/SakuraAnime/Downloads/" + "MALIMALI/" + animeTitle + "/";
        else
            savePath = Environment.getExternalStorageDirectory() + "/SakuraAnime/Downloads/" + "YHDM/" + animeTitle + "/";
        File dir = new File(savePath);
        if (!dir.exists()) dir.mkdirs();
        jsonObject = new JSONObject();
        jsonObject.put("title", animeTitle);
        jsonObject.put("source", source);
//        jsonObject.put("imomoePlaySource", 0);
        // m3u8下载配置
        m3U8VodOption = new M3U8VodOption();
        m3U8VodOption.ignoreFailureTs();
        m3U8VodOption.setUseDefConvert(false);
        m3U8VodOption.setBandWidthUrlConverter(new BandWidthUrlConverter());
        m3U8VodOption.setVodTsUrlConvert(new VodTsUrlConverter());
        m3U8VodOption.setMergeHandler(new TsMergeHandler());
        m3U8VodOption.generateIndexFile();
    }

    /**
     * 单集解析下载
     */
    private void downloadSingleParse(int position) {
        if (!isImomoe) {
            alertDialog = Utils.getProDialog(DescActivity.this, R.string.create_parse_download_task);
            createDownloadConfig();
            String yhdmUrl = downloadBean.get(position).getYhdmUrl();
            yhdmUrl = String.format(Api.PARSE_API, yhdmUrl);
            SniffingUtil.get().activity(this).referer(yhdmUrl).callback(this).url(yhdmUrl).position(position).start();
        } else {
            CustomToast.showToast(this, "不支持的下载格式", CustomToast.ERROR);
        }
    }

    /**
     * 下载所选的剧集
     */
    private void downloadSelected() {
        alertDialog = Utils.getProDialog(DescActivity.this, R.string.create_download_task);
        createDownloadConfig();
        for (int i=0,size=urls.size(); i<size; i++) {
            downloadVideoPresenter  = new DownloadVideoPresenter(urls.get(i), source, dramaNames.get(i), this);
            downloadVideoPresenter.loadData(false);
        }
    }

    /**
     * 获取yhdm视频下载地址
     * @param yhdmVideoUrlBean
     * @param playNumber
     */
    @Override
    public void showYhdmVideoSuccessView(YhdmVideoUrlBean yhdmVideoUrlBean, String playNumber) {
        runOnUiThread(() -> {
            String videoUrl = yhdmVideoUrlBean.getVidOrUrl();
            if (yhdmVideoUrlBean.isHttp()) {
                long taskId;
                String fileSavePath = savePath + playNumber;
                taskId = createDownloadTask(videoUrl.contains(".m3u8"), videoUrl, fileSavePath);
                DatabaseUtil.insertDownload(animeTitle, source, animeListBean.getImg(), sakuraUrl);
                DatabaseUtil.insertDownloadData(animeTitle, source, playNumber, 0, taskId);
                startDownloadService();
            } else {
                for (DownloadDramaBean downloadDramaBean : downloadBean) {
                    if (downloadDramaBean.getTitle().equals(playNumber)) {
                        downloadDramaBean.setYhdmUrl(videoUrl);
                        downloadDramaBean.setShouldParse(true);
                        downloadDramaBean.setSelected(false);
                        break;
                    }
                }
                Toast.makeText(getApplicationContext(), playNumber + " -> 下载出错，可能需要解析后才能下载~" , Toast.LENGTH_SHORT).show();
            }
            cancelDialog();
        });
    }

    /**
     * 获取下载地址失败
     * @param playNumber
     */
    @Override
    public void getVideoError(String playNumber) {
        runOnUiThread(() -> {
            String msg = isImomoe ? animeTitle : animeTitle + playNumber;
            Toast.makeText(getApplicationContext(),  msg + " -> 播放地址解析失败", Toast.LENGTH_SHORT).show();
            cancelDialog();
        });
    }

    /**
     * 获取Imomoe视频下载地址
     * @param url
     * @param playNumber
     */
    @Override
    public void showSuccessImomoeVideoUrlsView(String url, String playNumber) {
        runOnUiThread(() -> {
            long taskId;
            String fileSavePath = savePath + playNumber;
            taskId = createDownloadTask(url.endsWith(".m3u8"), url, fileSavePath);
            DatabaseUtil.insertDownload(animeTitle, source, animeListBean.getImg(), sakuraUrl);
            DatabaseUtil.insertDownloadData(animeTitle, source, playNumber, 0, taskId);
            Log.e("Malimali", animeTitle + "," + source + "," + animeListBean.getImg() + "," + sakuraUrl + ","+ playNumber + "," + taskId);
            startDownloadService();
            cancelDialog();
        });
    }
    /************************************************************ m3u8下载配置 START ************************************************************/
    static class BandWidthUrlConverter implements IBandWidthUrlConverter {
        @Override
        public String convert(String m3u8Url, String bandWidthUrl) {
            try {
                URL url = new URL(m3u8Url);
                m3u8Url = m3u8Url.replace(url.getPath(), "");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return m3u8Url + bandWidthUrl;
        }
    }

    static class VodTsUrlConverter implements IVodTsUrlConverter {
        @Override public List<String> convert(String m3u8Url, List<String> tsUrls) {
            // 转换ts文件的url地址
            try {
                URL url = new URL(m3u8Url);
                m3u8Url = m3u8Url.replace(url.getPath(), "").replaceAll("\\?.*", "");;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            List<String> newUrls = new ArrayList<>();
            for (String url : tsUrls) {
                newUrls.add(url.contains("http") ? url : m3u8Url + url);
            }
            return newUrls; // 返回有效的ts文件url集合
        }
    }

    static class TsMergeHandler implements ITsMergeHandler {
        public boolean merge(@Nullable M3U8Entity m3U8Entity, List<String> tsPath) {
            Log.e("TsMergeHandler", "合并TS....");
            String tsKey = m3U8Entity.getKeyPath() == null ? "" : VideoUtils.readKeyInfo(new File(m3U8Entity.getKeyPath()));
            byte[] tsIv = m3U8Entity.getIv() == null ? new byte[16] : m3U8Entity.getIv().getBytes();
            OutputStream outputStream = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            List<File> finishedFiles = new ArrayList<>();
            for (String path : tsPath) {
                try {
                    File pathFile = new File(path);
                    if (!tsKey.isEmpty()) {
                        Log.e("TsMergeHandler", "存在加密");
                        // 存在加密
                        inputStream= new FileInputStream(pathFile);
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes);
                        fileOutputStream = new FileOutputStream(pathFile);
                        // 解密ts片段
                        fileOutputStream.write(VideoUtils.decrypt(bytes, tsKey, tsIv));
                    }
                    finishedFiles.add(pathFile);
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) outputStream.close();
                        if (inputStream != null) inputStream.close();
                        if (fileOutputStream != null) fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return VideoUtils.merge(m3U8Entity.getFilePath(), finishedFiles);
        }
    }
    /************************************************************ m3u8下载配置 END ************************************************************/

    /**
     * 创建下载任务
     * @param isM3u8
     * @param url
     * @param savePath
     * @return
     */
    private long createDownloadTask(boolean isM3u8, String url, String savePath) {
        url = url.replaceAll("\\\\", "");
        Log.e("url", url);
        if (isM3u8)
            return Aria.download(this)
                    .load(url)
                    .setFilePath(savePath + ".m3u8")
                    .ignoreCheckPermissions()
                    .ignoreFilePathOccupy()
                    .setExtendField(jsonObject.toString())
                    .m3u8VodOption(m3U8VodOption)   // 调整下载模式为m3u8点播
                    .create();
         else
            return Aria.download(this)
                    .load(url)
                    .setFilePath(savePath + ".mp4")
                    .ignoreCheckPermissions()
                    .ignoreFilePathOccupy()
                    .setExtendField(jsonObject.toString())
                    .create();
    }

    /**
     * 开启下载服务
     */
    private void startDownloadService() {
        if (!Utils.isServiceRunning(this, "my.project.sakuraproject.services.DownloadService"))
            startService(new Intent(this, DownloadService.class));
    }

    /**
     * 检查是否在下载任务中
     */
    private void checkHasDownload() {
        List<DownloadDramaBean> data = downloadAdapter.getData();
        for (int i=0,size=data.size(); i<size; i++) {
            int complete = DatabaseUtil.queryDownloadDataIsDownloadError(animeId, data.get(i).getTitle(), 0);
            data.get(i).setSelected(false);
            switch (complete) {
                case -1:
                case 2:
                    data.get(i).setHasDownload(false);
                    break;
                default:
                    data.get(i).setHasDownload(true);
                    break;
            }
        }
        downloadAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSniffingStart(View webView, String url) {}

    @Override
    public void onSniffingFinish(View webView, String url) {
        cancelDialog();
        SniffingUtil.get().releaseWebView();
    }

    @Override
    public void onSniffingSuccess(View webView, String url, int position, List<SniffingVideo> videos) {
        List<String> urls = Utils.ridRepeat(videos);
        long taskId;
        DownloadDramaBean bean = downloadBean.get(position);
        if (urls.size() == 0) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), bean.getTitle() + " -> 下载出错，获取视频地址失败" , Toast.LENGTH_SHORT).show());
            return;
        }
        for (String videoUrl : urls) {
            taskId = createDownloadTask(videoUrl.contains(".m3u8"), videoUrl, savePath + bean.getTitle());
            DatabaseUtil.insertDownload(animeTitle, source, animeListBean.getImg(), sakuraUrl);
            DatabaseUtil.insertDownloadData(animeTitle, source, bean.getTitle(), 0, taskId);
            downloadBean.get(position).setShouldParse(false);
            downloadBean.get(position).setHasDownload(true);
            downloadAdapter.notifyItemChanged(position);
            startDownloadService();
            return;
        }
    }

    @Override
    public void onSniffingError(View webView, String url, int position, int errorCode) {
        DownloadDramaBean bean = downloadBean.get(position);
        bean.setShouldParse(false);
        downloadAdapter.notifyItemChanged(position);
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), bean.getTitle() + " -> 下载出错，解析视频地址失败" , Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewView() {
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad()) {
            lineRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            downloadRecyclerView.setLayoutManager(new GridLayoutManager(this,  4));
        }
        else {
            if (isInMagicWindow) {
                lineRecyclerView.setLayoutManager(new GridLayoutManager(this, 6));
                downloadRecyclerView.setLayoutManager(new GridLayoutManager(this, 6));
            } else {
                lineRecyclerView.setLayoutManager(new GridLayoutManager(this, 8));
                downloadRecyclerView.setLayoutManager(new GridLayoutManager(this, 8));
            }
        }
    }
}
