package my.project.sakuraproject.main.desc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.AnimeDescMultiRecommendAdapter;
import my.project.sakuraproject.adapter.DownloadDramaAdapter;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeDramasBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.DownloadDramaBean;
import my.project.sakuraproject.bean.Event;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.config.M3U8DownloadConfig;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.search.SearchActivity;
import my.project.sakuraproject.main.video.DownloadVideoContract;
import my.project.sakuraproject.main.video.DownloadVideoPresenter;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.services.DownloadService;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class DescActivity extends BaseActivity<DescContract.View, DescPresenter> implements DescContract.View, VideoContract.View,
        DownloadVideoContract.View {
    /**
     * appBarLayout
     */
    @BindView(R.id.app_bar_margin)
    LinearLayout appBarMargin;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    /**
     * 收藏按钮视图
     */
    @BindView(R.id.favorite)
    MaterialButton favoriteBtn;
    /**
     * 番剧图片视图
     */
    @BindView(R.id.anime_img)
    ImageView animeImgView;
    /**
     * 番剧详情视图
     */
    @BindView(R.id.desc)
    ExpandableTextView expandableDescView;
    /**
     * 番剧标题视图
     */
    @BindView(R.id.title)
    TextView titleView;
    /**
     * 下拉刷新视图
     */
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    /**
     * detailsTitle 详情标题
     * detailsUrl 详情源地址
     * dramaUrl 播放页源地址
     * dramaTitle 播放集数标题
     */
    private String detailsTitle, detailsUrl, dramaUrl, dramaTitle;
    private AlertDialog alertDialog;
    /**
     * 是否收藏
     */
    private boolean isFavorite;
    private VideoPresenter videoPresenter;
    private AnimeListBean animeListBean = new AnimeListBean();
    /**
     * 记住点击过的番剧 用于手势返回时回到上以番剧信息
     */
    private List<String> animeUrlList = new ArrayList();
    /**
     * 包含 播放列表、多季、相关推荐
     */
    private AnimeDescListBean animeDescListBean = new AnimeDescListBean();
    /**
     * 剧集列表
     */
    private List<AnimeDescDetailsBean> dramaList;
    /**
     * 播放列表相关
     */
    @BindView(R.id.drama_list)
    RecyclerView dramaListRv;
    private LinearLayoutManager dramaListLayoutManager;
    private DramaAdapter dramaListAdapter;
    @BindView(R.id.play_layout)
    LinearLayout playLinearLayout;
    /**
     * 展开播放列表相关
     */
    private RecyclerView expandListRv;
    private DramaAdapter expandListAdapter;
    private ImageView closeDrama;
    private BottomSheetDialog expandListBSD;
    @BindView(R.id.open_drama)
    RelativeLayout openDramaView;
    /**
     * 多季相关
     */
    @BindView(R.id.multi_list)
    RecyclerView multiListRv;
    private LinearLayoutManager multiLayoutManager;
    private AnimeDescMultiRecommendAdapter multiAdapter;
    @BindView(R.id.multi_layout)
    LinearLayout multiLinearLayout;
    /**
     * 相关推荐相关
     */
    @BindView(R.id.recommend_list)
    RecyclerView recommendRv;
    private LinearLayoutManager recommendLayoutManager;
    private AnimeDescMultiRecommendAdapter recommendAdapter;
    @BindView(R.id.recommend_layout)
    LinearLayout recommendLinearLayout;
    /**
     * 加载错误视图相关
     */
    @BindView(R.id.error_bg)
    RelativeLayout errorBgView;
    @BindView(R.id.error_msg)
    TextView errorMsgView;
    /**
     * 详情视图
     */
    @BindView(R.id.desc_view)
    RelativeLayout descView;
    /**
     * 背景模糊图片
     */
    @BindView(R.id.bg)
    ImageView bgView;
    /**
     * TAG列表
     */
    @BindView(R.id.chip_group)
    ChipGroup chipGroupView;
    /**
     * 更新时间视图
     */
    @BindView(R.id.update_time)
    TextView updateTimeView;
    /**
     * 评分视图
     */
    @BindView(R.id.score_view)
    AppCompatTextView scoreView;
    /**
     * 整体滚动视图
     */
    @BindView(R.id.scrollview)
    NestedScrollView scrollView;
    /**
     * 是否是S站
     */
    private boolean isImomoe;
    /**
     * 当前点击剧集
     */
    private int clickIndex;
    /**
     * 番剧ID
     */
    private String animeId;
    /**
     * 下载相关参数
     */
    private String savePath; // 下载保存路劲
    private int source; // 番剧源 0 yhdm 1 silisili
    private DownloadVideoPresenter downloadVideoPresenter; // 下载适配器
    private RecyclerView downloadRecyclerView;
    private BottomSheetDialog downloadBottomSheetDialog;
    private List<DownloadDramaBean> downloadBean = new ArrayList<>();
    private DownloadDramaAdapter downloadAdapter;
    private M3U8VodOption m3U8VodOption; // 下载m3u8配置
    /**
     * 多播放列表选择相关
     */
    @BindView(R.id.selected_text)
    AutoCompleteTextView selectedDrama;
    private List<String> dramaTitles;
    private ArrayAdapter dramaTitlesApter;
    private int sourceIndex = 0; // 所选播放列表下标

    @Override
    protected DescPresenter createPresenter() {
        return new DescPresenter(detailsUrl, this);
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
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0);
        if (isDarkTheme) bgView.setVisibility(View.GONE);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> mSwipe.setEnabled(scrollView.getScrollY() == 0));
        expandableDescView.setNeedExpend(true);
        getBundle();
        initToolbar();
        initSwipe();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {}

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            detailsUrl = bundle.getString("url");
            detailsTitle = bundle.getString("name");
            animeUrlList.add(detailsUrl);
        }
    }

    private void initToolbar() {
        appBarMargin.setOnApplyWindowInsetsListener((v, insets) -> {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) appBarMargin.getLayoutParams();
            layoutParams.setMargins(0, insets.getSystemWindowInsetTop(), 0, 0);
            appBarMargin.setLayoutParams(layoutParams);
            return insets;
        });
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    @OnClick({R.id.order, R.id.favorite, R.id.download, R.id.browser})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.order:
                Collections.reverse(dramaList);
                dramaListAdapter.notifyDataSetChanged();
                expandListAdapter.notifyDataSetChanged();
                break;
            case R.id.favorite:
                favoriteAnime();
                break;
            case R.id.download:
                if (downloadAdapter.getData().size() == 0)
                    return;
                checkHasDownload();
                downloadBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                downloadBottomSheetDialog.show();
                break;
            case R.id.browser:
                Utils.viewInChrome(this, BaseModel.getDomain(isImomoe) + detailsUrl);
                break;
        }
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setProgressViewOffset(true, -0, 150);
        mSwipe.setOnRefreshListener(() ->  {
            sourceIndex = 0;
            mSwipe.setRefreshing(true);
            mPresenter.loadData(true);
        });
    }

    @SuppressLint("RestrictedApi")
    public void initAdapter() {
        dramaListAdapter = new DramaAdapter(this, dramaList);
        dramaListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            playVideo(adapter, position, dramaListRv);
        });
        dramaListLayoutManager = getLinearLayoutManager();
        dramaListRv.setLayoutManager(dramaListLayoutManager);
        dramaListRv.setAdapter(dramaListAdapter);
        dramaListRv.setNestedScrollingEnabled(false);

        multiAdapter = new AnimeDescMultiRecommendAdapter(this, animeDescListBean.getAnimeDescMultiBeans());
        multiAdapter.setOnItemClickListener((adapter, view, position) -> {
            AnimeDescRecommendBean bean = (AnimeDescRecommendBean) adapter.getItem(position);
            detailsTitle = bean.getTitle();
            detailsUrl = bean.getUrl();
            animeUrlList.add(detailsUrl);
            openAnimeDesc();
        });
        multiLayoutManager = getLinearLayoutManager();
        multiListRv.setLayoutManager(multiLayoutManager);
        multiListRv.setAdapter(multiAdapter);
        multiListRv.setNestedScrollingEnabled(false);

        recommendAdapter = new AnimeDescMultiRecommendAdapter(this, animeDescListBean.getAnimeDescRecommendBeans());
        recommendAdapter.setOnItemClickListener((adapter, view, position) -> {
            AnimeDescRecommendBean bean = (AnimeDescRecommendBean) adapter.getItem(position);
            detailsTitle = bean.getTitle();
            detailsUrl = bean.getUrl();
            animeUrlList.add(detailsUrl);
            openAnimeDesc();
        });
        recommendLayoutManager = getLinearLayoutManager();
        recommendRv.setLayoutManager(recommendLayoutManager);
        if (Utils.checkHasNavigationBar(this)) recommendRv.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        recommendRv.setAdapter(recommendAdapter);
        recommendRv.setNestedScrollingEnabled(false);

        View dramaView = LayoutInflater.from(this).inflate(R.layout.dialog_drama, null);
        expandListRv = dramaView.findViewById(R.id.drama_list);

        expandListAdapter = new DramaAdapter(this, new ArrayList<>());
        expandListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            expandListBSD.dismiss();
            playVideo(adapter, position, expandListRv);
        });
        expandListRv.setAdapter(expandListAdapter);
        expandListBSD = new BottomSheetDialog(this);
        expandListBSD.setContentView(dramaView);
        closeDrama = dramaView.findViewById(R.id.close_drama);
        closeDrama.setOnClickListener(v-> expandListBSD.dismiss());

        View downloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_drama, null);
        ExpandableTextView expandableTextView = downloadView.findViewById(R.id.info);
        expandableTextView.setContent(Utils.getString(R.string.download_info));
        expandableTextView.setNeedExpend(true);
        downloadRecyclerView = downloadView.findViewById(R.id.download_list);

        downloadAdapter = new DownloadDramaAdapter(this, new ArrayList<>());
        downloadAdapter.setOnItemClickListener((adapter, view, position) -> {
            DownloadDramaBean bean = downloadBean.get(position);
            if (bean.isHasDownload()) {
                CustomToast.showToast(this, "已存在下载任务，请勿重复执行！", CustomToast.WARNING);
                return;
            }
            // 下载开始
            alertDialog = Utils.getProDialog(DescActivity.this, R.string.create_download_task);
            createDownloadConfig();
            downloadVideoPresenter  = new DownloadVideoPresenter(downloadBean.get(position).getUrl(), source, downloadBean.get(position).getTitle(), this);
            downloadVideoPresenter.loadData(false);
        });
        downloadRecyclerView.setAdapter(downloadAdapter);
        downloadBottomSheetDialog = new BottomSheetDialog(this);
        downloadBottomSheetDialog.setContentView(downloadView);
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        return linearLayoutManager;
    }

    private void chipClick(int position) {
        Bundle bundle = new Bundle();
        isImomoe = detailsUrl.contains("/voddetail/");
        bundle.putBoolean("isImomoe", isImomoe);
        if (!isImomoe) {
            bundle.putString("title", animeListBean.getTagTitles().get(position));
            bundle.putString("url", animeListBean.getTagUrls().get(position));
            bundle.putBoolean("isMovie", animeListBean.getTagUrls().get(position).contains("movie") ? true : false);
            startActivity(new Intent(DescActivity.this, AnimeListActivity.class).putExtras(bundle));
        }
        else {
            bundle.putString("title", animeListBean.getTagUrls().get(position).replaceAll("/vodsearch", ""));
            bundle.putString("siliToolbarTitle", animeListBean.getTagTitles().get(position));
            bundle.putBoolean("isSiliTag", true);
            startActivity(new Intent(DescActivity.this, SearchActivity.class).putExtras(bundle));
        }
    }

    @OnClick(R.id.open_drama)
    public void dramaClick() {
        if (!expandListBSD.isShowing()) {
            expandListBSD.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            expandListBSD.show();
        }
    }

    @SuppressLint("RestrictedApi")
    public void openAnimeDesc() {
//        downloadView.setVisibility(View.GONE);
        animeImgView.setImageDrawable(getDrawable(isDarkTheme ? R.drawable.loading_night : R.drawable.loading_light));
        hideView(chipGroupView);
        chipGroupView.removeAllViews();
        hideView(scoreView);
        setTextviewEmpty(expandableDescView);
        animeDescListBean = new AnimeDescListBean();
//        hideViewInvisible(bottomAppBar);
        bgView.setImageDrawable(getResources().getDrawable(R.drawable.default_bg));
        mPresenter = new DescPresenter(detailsUrl, this);
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
        String playNumber;
        dramaList.get(position).setSelected(true);
        dramaUrl = dramaList.get(position).getUrl();
        playNumber = dramaList.get(position).getTitle();
        dramaTitle = detailsTitle + " - " + playNumber;
        dramaListAdapter.notifyDataSetChanged();
        expandListAdapter.notifyDataSetChanged();
        videoPresenter = new VideoPresenter(detailsTitle, dramaUrl, sourceIndex, playNumber, DescActivity.this);
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
                VideoUtils.openPlayer(true, this, dramaTitle, animeUrl, detailsTitle, dramaUrl, dramaList, clickIndex, animeId, sourceIndex,  isImomoe);
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
                detailsUrl = animeUrlList.get(animeUrlList.size() - 1);
                openAnimeDesc();
            } else
                CustomToast.showToast(this, Utils.getString(R.string.load_desc_info), CustomToast.WARNING);
        }
    }

    public void favoriteAnime() {
        isFavorite = DatabaseUtil.favorite(animeListBean, animeId);
        favoriteBtn.setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_favorite_white_48dp : R.drawable.baseline_favorite_border_white_48dp));
        favoriteBtn.setText(isFavorite ? Utils.getString(R.string.has_favorite) : Utils.getString(R.string.favorite));
        application.showSnackbarMsg(favoriteBtn, isFavorite ? Utils.getString(R.string.join_ok) : Utils.getString(R.string.join_error), playLinearLayout);
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
                .into(bgView);
        animeImgView.setTag(R.id.imageid, animeListBean.getImg());
        Utils.setDefaultImage(this, animeListBean.getImg(), animeListBean.getUrl(), animeImgView, false, null, null);
        titleView.setText(animeListBean.getTitle());
        if (animeListBean.getTagTitles() != null) {
            for (int i=0,size=animeListBean.getTagTitles().size(); i<size; i++) {
                Chip chip = new Chip(this);
                chip.setText(animeListBean.getTagTitles().get(i));
                chip.setBackgroundColor(getResources().getColor(R.color.window_bg));
                chip.setTextColor(getResources().getColor(R.color.text_color_primary));
                chip.setChipStrokeColorResource(R.color.head);
                chip.setRippleColor(getResources().getColorStateList(R.color.ripple_color));
                int position = i;
                chip.setOnClickListener(view -> {
                    chipClick(position);
                });
                chipGroupView.addView(chip);
            }
            showView(chipGroupView);
        }else
            hideView(chipGroupView);
        if (animeListBean.getDesc() == null || animeListBean.getDesc().isEmpty())
            hideView(expandableDescView);
        else {
            expandableDescView.setContent(animeListBean.getDesc());
            showView(expandableDescView);
        }
        updateTimeView.setText("·"+animeListBean.getUpdateTime());
        scoreView.setText("·"+animeListBean.getScore()+ (isImomoe ? "" : "分"));
        showView(scoreView);
    }

    @Override
    public void showLoadingView() {
        showEmptyVIew();
        dramaListRv.scrollToPosition(0);
        multiListRv.scrollToPosition(0);
        recommendRv.scrollToPosition(0);
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                hideView(descView);
                hideView(playLinearLayout);
                hideView(multiLinearLayout);
                hideView(recommendLinearLayout);
                errorMsgView.setText(msg);
                showView(errorBgView);
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
        hideView(descView);
        hideView(playLinearLayout);
        hideView(multiLinearLayout);
        hideView(recommendLinearLayout);
        hideView(errorBgView);
    }

    @Override
    public void showLog(String url) {
//        runOnUiThread(() -> application.showToastShortMsg(url));
    }

    @Override
    public void showSuccessMainView(AnimeDescListBean bean) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                /*if (!isImomoe)
                    downloadView.setVisibility(View.VISIBLE);*/
                setCollapsingToolbar();
                mSwipe.setRefreshing(false);
                if (isFavorite) DatabaseUtil.updateFavorite(animeListBean, animeId);
                animeDescListBean = bean;
                downloadBean = new ArrayList<>();
                // 默认展示第一播放源
                dramaList = animeDescListBean.getAnimeDramasBeans().get(0).getAnimeDescDetailsBeanList();
                dramaTitles = new ArrayList<>();
                for (AnimeDramasBean animeDramasBean : animeDescListBean.getAnimeDramasBeans()) {
                    dramaTitles.add(animeDramasBean.getListTitle());
                }
                initTitleAdapter();
                setAdapterData(0);
                if (bean.getAnimeDescMultiBeans().size() > 0)
                    showView(multiLinearLayout);
                else
                    hideView(multiLinearLayout);
                multiAdapter.setNewData(bean.getAnimeDescMultiBeans());
                recommendAdapter.setNewData(bean.getAnimeDescRecommendBeans());
                showView(descView);
                showView(playLinearLayout);
                showView(recommendLinearLayout);
            }
        });
    }

    private void initTitleAdapter() {
        selectedDrama.setText(dramaTitles.get(0));
        dramaTitlesApter = new ArrayAdapter(this, R.layout.list_item, dramaTitles);
        selectedDrama.setAdapter(dramaTitlesApter);
        selectedDrama.setOnItemClickListener((parent, view, position, id) -> {
            setAdapterData(position);
        });
    }

    private void setAdapterData(int position) {
        sourceIndex = position;
        dramaList = animeDescListBean.getAnimeDramasBeans().get(position).getAnimeDescDetailsBeanList();
        dramaListAdapter.setNewData(dramaList);
        downloadBean = new ArrayList<>();
        for (AnimeDescDetailsBean b : dramaList) {
            DownloadDramaBean downloadDramaBean = new DownloadDramaBean();
            downloadDramaBean.setTitle(b.getTitle());
            downloadDramaBean.setSelected(false);
            downloadDramaBean.setUrl(b.getUrl());
            downloadBean.add(downloadDramaBean);
        }
        setAnimeDescDramaAdapter();
    }

    private void setAnimeDescDramaAdapter() {
        showView(openDramaView);
        expandListAdapter.setNewData(dramaList);
        downloadAdapter.setNewData(downloadBean);
    }

    @Override
    public void showSuccessDescView(AnimeListBean bean) {
        animeListBean = bean;
        detailsTitle = animeListBean.getTitle();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void showSuccessFavorite(boolean is) {
        isFavorite = is;
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                favoriteBtn.setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_favorite_white_48dp : R.drawable.baseline_favorite_border_white_48dp));
                favoriteBtn.setText(isFavorite ? Utils.getString(R.string.has_favorite) : Utils.getString(R.string.favorite));
            }
        });
    }

    @Override
    public void showEmptyDram(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                setCollapsingToolbar();
                showView(descView);
                hideView(playLinearLayout);
                hideView(multiLinearLayout);
                hideView(recommendLinearLayout);
                errorMsgView.setText(msg);
                showView(errorBgView);
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
            VideoUtils.showErrorInfo(this, dramaUrl, false);
//            CustomToast.showToast(this, Utils.getString(R.string.error_600), CustomToast.ERROR);
        });
    }

    @Override
    public void getVideoError() {
        runOnUiThread(() -> {
            VideoUtils.showErrorInfo(this, dramaUrl, false);
//            CustomToast.showToast(this, Utils.getString(R.string.error_600), CustomToast.ERROR);
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
        dramaList.get(clickIndex).setSelected(true);
        dramaListAdapter.notifyDataSetChanged();
        expandListAdapter.notifyDataSetChanged();
    }

    /****************************************** 下载相关 ******************************************/
    /**
     * 下载配置
     */
    private void createDownloadConfig() {
        if (!Utils.hasFilePermission())
            return;
        if (isImomoe)
            savePath = Environment.getExternalStorageDirectory() + "/SakuraAnime/Downloads/SILISILI/" + detailsTitle + "/";
        else
            savePath = Environment.getExternalStorageDirectory() + "/SakuraAnime/Downloads/YHDM/" + detailsTitle + "/";
        File dir = new File(savePath);
        if (!dir.exists()) dir.mkdirs();
        // m3u8下载配置
        m3U8VodOption = new M3U8VodOption();
        m3U8VodOption.ignoreFailureTs();
        m3U8VodOption.setUseDefConvert(false);
        m3U8VodOption.setBandWidthUrlConverter(new M3U8DownloadConfig.BandWidthUrlConverter());
        m3U8VodOption.setVodTsUrlConvert(new M3U8DownloadConfig.VodTsUrlConverter());
        m3U8VodOption.setMergeHandler(new M3U8DownloadConfig.TsMergeHandler());
//        m3U8VodOption.generateIndexFile();
    }

    /**
     * 获取yhdm视频下载地址
     * @param urls
     * @param playNumber
     */
    @Override
    public void showYhdmVideoSuccessView(List<String> urls, String playNumber) {
        runOnUiThread(() -> {
            cancelDialog();
            if (urls.size() > 1) {
                VideoUtils.showMultipleVideoSources4Download(this,
                        urls,
                        (dialog, index) -> {
                            startDownload(urls.get(index), playNumber);
                            dialog.dismiss();
                        }
                );
            } else
                startDownload(urls.get(0), playNumber);
        });
    }

    /**
     * 开始执行下载操作
     * @param url
     * @param playNumber
     */
    private void startDownload(String url, String playNumber) {
        if (!Utils.hasFilePermission()) {
            Utils.openOtherSoftware(this, url);
            return;
        }
        if (!url.contains("http")) {
            VideoUtils.showInfoDialog(this, "不支持的下载格式，该地址可能非视频地址！ -> " + url);
            return;
        }
        long taskId;
        String fileSavePath = savePath + playNumber;
        boolean isM3U8 = url.endsWith("m3u8");
        taskId = createDownloadTask(isM3U8, url, fileSavePath);
        if (isM3U8) VideoUtils.showInfoDialog(this, "该视频资源类型为M3U8，可能无法正常下载成功！");
        DatabaseUtil.insertDownload(detailsTitle, source, animeListBean.getImg(), detailsUrl);
        DatabaseUtil.insertDownloadData(detailsTitle, source, playNumber, 0, taskId);
        Toast.makeText(this, "开始下载 -> " +playNumber, Toast.LENGTH_SHORT).show();
        // 开启下载服务
        startService(new Intent(this, DownloadService.class));
        EventBus.getDefault().post(new Refresh(3));
        checkHasDownload();
    }

    /**
     * 获取下载地址失败
     * @param playNumber
     */
    @Override
    public void getVideoError(String playNumber) {
        runOnUiThread(() -> {
            String msg = isImomoe ? detailsTitle : detailsTitle + playNumber;
            cancelDialog();
            VideoUtils.showInfoDialog(this, msg + " -> 播放地址解析失败");
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
            cancelDialog();
            startDownload(url, playNumber);
        });
    }

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
                    .m3u8VodOption(m3U8VodOption)   // 调整下载模式为m3u8点播
                    .create();
         else
            return Aria.download(this)
                    .load(url)
                    .setFilePath(savePath + ".mp4")
                    .ignoreCheckPermissions()
                    .ignoreFilePathOccupy()
                    .create();
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
    public void onResume() {
        super.onResume();
        setRecyclerViewView();
    }

    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewView() {
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad()) {
            expandListRv.setLayoutManager(new GridLayoutManager(this, 4));
            downloadRecyclerView.setLayoutManager(new GridLayoutManager(this,  4));
        }
        else {
            if (isInMagicWindow) {
                expandListRv.setLayoutManager(new GridLayoutManager(this, 6));
                downloadRecyclerView.setLayoutManager(new GridLayoutManager(this, 6));
            } else {
                expandListRv.setLayoutManager(new GridLayoutManager(this, 8));
                downloadRecyclerView.setLayoutManager(new GridLayoutManager(this, 8));
            }
        }
    }
}
