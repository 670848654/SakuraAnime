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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSONObject;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
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
import my.project.sakuraproject.bean.AnimeDramasBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.DownloadDramaBean;
import my.project.sakuraproject.bean.Event;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.YhdmVideoUrlBean;
import my.project.sakuraproject.config.M3U8DownloadConfig;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.BaseModel;
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
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class DescActivity extends BaseActivity<DescContract.View, DescPresenter> implements DescContract.View, VideoContract.View,
        DownloadVideoContract.View, SniffingUICallback {
    @BindView(R.id.app_bar_margin)
    LinearLayout appBarMargin;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.favorite)
    MaterialButton favoriteBtn;
    @BindView(R.id.anime_img)
    ImageView animeImg;
    @BindView(R.id.desc)
    ExpandableTextView desc;
    @BindView(R.id.title)
    TextView title;
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
    private RecyclerView lineRecyclerView;
    private AnimeDescDetailsAdapter animeDescDetailsAdapter;
    private AnimeDescRecommendAdapter animeDescRecommendAdapter;
    private AnimeDescRecommendAdapter animeDescMultiAdapter;
    private AnimeDescListBean animeDescListBean = new AnimeDescListBean();
    private List<AnimeDescDetailsBean> dramaList; // 剧集列表
    private ImageView closeDrama;
    private BottomSheetDialog mBottomSheetDialog;
    private AnimeDescDramaAdapter animeDescDramaAdapter;
    @BindView(R.id.desc_view)
    RelativeLayout desc_view;
    @BindView(R.id.bg)
    ImageView bg;
    /*@BindView(R.id.tag_view)
    TagContainerLayout tagContainerLayout;*/
    @BindView(R.id.chip_group)
    ChipGroup chipGroup;
    @BindView(R.id.update_time)
    TextView update_time;
    @BindView(R.id.score_view)
    AppCompatTextView score_view;
    @BindView(R.id.scrollview)
    NestedScrollView scrollView;
    private boolean isImomoe;
    private int clickIndex; // 当前点击剧集
    // 番剧ID
    private String animeId;
    /*@BindView(R.id.to_back)
    FloatingActionButton toBackView;*/
//    private SlidrInterface slidrInterface;
    // 下载
    private String savePath; // 下载保存路劲
    private int source; // 番剧源 0 yhdm 1 imomoe
    private DownloadVideoPresenter downloadVideoPresenter; // 下载适配器
    private JSONObject jsonObject; // Aria任务扩展数据
    private RecyclerView downloadRecyclerView;
    private BottomSheetDialog downloadBottomSheetDialog;
    private List<DownloadDramaBean> downloadBean = new ArrayList<>();
    private DownloadDramaAdapter downloadAdapter;
    private ExtendedFloatingActionButton downloadFab;
    List<String> urls; // 保存
    List<String> dramaNames;
    private M3U8VodOption m3U8VodOption; // 下载m3u8配置

    @BindView(R.id.selected_text)
    AutoCompleteTextView selectedDrama;
    private List<String> dramaTitles;
    private ArrayAdapter dramaTitlesApter;

    /*@BindView(R.id.bottomAppBar)
    BottomAppBar bottomAppBar;*/

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
//        StatusBarUtil.setColorForSwipeBack(this, getResources().getColor(R.color.translucent), 0);
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0);
        if (isDarkTheme) bg.setVisibility(View.GONE);
//        slidrInterface = Slidr.attach(this, Utils.defaultInit());
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> mSwipe.setEnabled(scrollView.getScrollY() == 0));
        desc.setNeedExpend(true);
        getBundle();
        initToolbar();
//        initBottomAppBar();
        initSwipe();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            sakuraUrl = bundle.getString("url");
            animeTitle = bundle.getString("name");
            animeUrlList.add(sakuraUrl);
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

    @OnClick({R.id.favorite, R.id.download, R.id.browser})
    public void onClick(MaterialButton view) {
        switch (view.getId()) {
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
                Utils.viewInChrome(this, BaseModel.getDomain(isImomoe) + sakuraUrl);
                break;
        }
    }

    /*public void initBottomAppBar() {
        if (Utils.checkHasNavigationBar(this)) {
            bottomAppBar.setPadding(0, 0, 0, Utils.getNavigationBarHeight());
        }
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.favorite:
                    favoriteAnime();
                    break;
                case R.id.download:
                    if (downloadAdapter.getData().size() == 0)
                        return false;
                    checkHasDownload();
                    downloadBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                    downloadBottomSheetDialog.show();
                    break;
                case R.id.browser:
                    Utils.viewInChrome(this, BaseModel.getDomain(isImomoe) + sakuraUrl);
                    break;
            }
            return true;
        });
    }*/

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
        animeDescDetailsAdapter = new AnimeDescDetailsAdapter(this, dramaList);
        animeDescDetailsAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            playVideo(adapter, position, detailsRv);
        });
        detailsRvLinearLayoutManager = getLinearLayoutManager();
        detailsRv.setLayoutManager(detailsRvLinearLayoutManager);
        detailsRv.setAdapter(animeDescDetailsAdapter);
        detailsRv.setNestedScrollingEnabled(false);
//        setEnableSliding(detailsRv, detailsRvLinearLayoutManager);

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
//        setEnableSliding(multiRv, multiRvLinearLayoutManager);

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
//        setEnableSliding(recommendRv, recommendRvLinearLayoutManager);

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
        downloadFab = downloadView.findViewById(R.id.download);
        downloadFab.post(() -> {
            int height = downloadFab.getHeight();
            downloadRecyclerView.setPadding(0, 0, 0, height + height/2);
        });
        downloadFab.setOnClickListener(v -> {
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

    /*private void setEnableSliding(RecyclerView recommendRv, LinearLayoutManager linearLayoutManager) {
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
    }*/

    private void chipClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("title", animeListBean.getTagTitles().get(position));
        bundle.putString("url", animeListBean.getTagUrls().get(position));
        bundle.putBoolean("isMovie", animeListBean.getTagUrls().get(position).contains("movie") ? true : false);
        isImomoe = sakuraUrl.contains("/voddetail/");
        bundle.putBoolean("isImomoe", isImomoe);
        if (!isImomoe)
            startActivity(new Intent(DescActivity.this, AnimeListActivity.class).putExtras(bundle));
        else
            CustomToast.showToast(DescActivity.this, "当前源不支持", CustomToast.WARNING);
    }

    @OnClick(R.id.open_drama)
    public void dramaClick() {
        if (!mBottomSheetDialog.isShowing()) {
            mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            mBottomSheetDialog.show();
        }
    }

    @SuppressLint("RestrictedApi")
    public void openAnimeDesc() {
//        downloadView.setVisibility(View.GONE);
        animeImg.setImageDrawable(getDrawable(isDarkTheme ? R.drawable.loading_night : R.drawable.loading_light));
        hideView(chipGroup);
        chipGroup.removeAllViews();
        hideView(score_view);
        setTextviewEmpty(desc);
        animeDescListBean = new AnimeDescListBean();
//        hideViewInvisible(bottomAppBar);
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
        dramaList.get(position).setSelected(true);
        dramaUrl = dramaList.get(position).getUrl();
        playNumber = dramaList.get(position).getTitle();
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
                VideoUtils.openPlayer(true, this, witchTitle, animeUrl, animeTitle, dramaUrl, dramaList, clickIndex, animeId, isImomoe);
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
        /*bottomAppBar.getMenu().getItem(0).setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_favorite_white_48dp : R.drawable.baseline_favorite_border_white_48dp));
        bottomAppBar.getMenu().getItem(0).setTitle(isFavorite ? Utils.getString(R.string.has_favorite) : Utils.getString(R.string.favorite));
        application.showSnackbarMsg(bottomAppBar, isFavorite ? Utils.getString(R.string.join_ok) : Utils.getString(R.string.join_error), toBackView);*/
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
                .into(bg);
        Utils.setDefaultImage(this, animeListBean.getImg(), animeListBean.getUrl(), animeImg, false, null, null);
        title.setText(animeListBean.getTitle());
        if (animeListBean.getTagTitles() != null) {
            for (int i=0,size=animeListBean.getTagTitles().size(); i<size; i++) {
                Chip chip = new Chip(this);
                chip.setText(animeListBean.getTagTitles().get(i));
                chip.setBackgroundColor(getResources().getColor(R.color.window_bg));
                chip.setTextColor(getResources().getColor(R.color.text_color_primary));
                chip.setChipStrokeColorResource(R.color.head);
                int position = i;
                chip.setOnClickListener(view -> {
                    chipClick(position);
                });
                chipGroup.addView(chip);
            }
            showView(chipGroup);
        }else
            hideView(chipGroup);
        if (animeListBean.getDesc() == null || animeListBean.getDesc().isEmpty())
            hideView(desc);
        else {
            desc.setContent(animeListBean.getDesc());
            showView(desc);
        }
        update_time.setText("·"+animeListBean.getUpdateTime());
        if (!isImomoe) {
            score_view.setText("·"+animeListBean.getScore()+"分");
            showView(score_view);
        }
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
                mSwipe.setRefreshing(false);
//                hideViewInvisible(bottomAppBar);
                hideView(desc_view);
                hideView(playLinearLayout);
                hideView(multiLinearLayout);
                hideView(recommendLinearLayout);
                error_msg.setText(msg);
                showView(errorBg);
            }
        });
    }

    /*private void hideViewInvisible(View view) {
        Utils.fadeOut(view);
        view.setVisibility(View.INVISIBLE);
    }*/

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
//        hideViewInvisible(bottomAppBar);
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
                animeDescMultiAdapter.setNewData(bean.getAnimeDescMultiBeans());
                animeDescRecommendAdapter.setNewData(bean.getAnimeDescRecommendBeans());
//                showView(bottomAppBar);
                showView(desc_view);
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
        dramaList = animeDescListBean.getAnimeDramasBeans().get(position).getAnimeDescDetailsBeanList();
        animeDescDetailsAdapter.setNewData(dramaList);
        animeDescDetailsAdapter.setNewData(dramaList);
        downloadBean = new ArrayList<>();
        for (AnimeDescDetailsBean b : dramaList) {
            DownloadDramaBean downloadDramaBean = new DownloadDramaBean();
            downloadDramaBean.setTitle(b.getTitle());
            downloadDramaBean.setSelected(false);
            downloadDramaBean.setUrl(b.getUrl());
            downloadBean.add(downloadDramaBean);
        }
        setAnimeDescDramaAdapter(0);
    }

    private void setAnimeDescDramaAdapter(int sourceIndex) {
        if (dramaList.size() > 4)
            showView(openDrama);
        else
            hideView(openDrama);
        animeDescDramaAdapter.setNewData(dramaList);
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
                /*bottomAppBar.getMenu().getItem(0).setIcon(ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_favorite_white_48dp : R.drawable.baseline_favorite_border_white_48dp));
                bottomAppBar.getMenu().getItem(0).setTitle(isFavorite ? Utils.getString(R.string.has_favorite) : Utils.getString(R.string.favorite));*/
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
                showView(desc_view);
//                showView(bottomAppBar);
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
        dramaList.get(clickIndex).setSelected(true);
        animeDescDetailsAdapter.notifyDataSetChanged();
        animeDescDramaAdapter.notifyDataSetChanged();
    }

    /****************************************** 下载相关 ******************************************/
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
        jsonObject.put("imomoePlaySource", 0);
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
     * 单集解析下载
     */
    private void downloadSingleParse(int position) {
        alertDialog = Utils.getProDialog(DescActivity.this, R.string.create_parse_download_task);
        createDownloadConfig();
        if (!isImomoe) {
            String url = String.format(Api.PARSE_API, downloadBean.get(position).getYhdmUrl());
            SniffingUtil.get().activity(this).referer(url).callback(this).url(url).position(position).start();
        } else {
            VideoUtils.showParseAlert(this, (dialog, index) -> {
                dialog.dismiss();
                String url = String.format(Api.PARSE_INTERFACE[index], downloadBean.get(position).getYhdmUrl());
                if (index == Api.PARSE_INTERFACE.length -1) {
                    cancelDialog();
                    Toast.makeText(this, "该接口仅用于WebView播放！", Toast.LENGTH_SHORT).show();
                } else  {
                    SniffingUtil.get().activity(this).referer(url).callback(this).url(url).start();
                    Toast.makeText(this, Utils.getString(R.string.select_parse_interface_msg), Toast.LENGTH_LONG).show();
                }
            });
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
                Toast.makeText(getApplicationContext(), playNumber + " -> 下载出错，可能需要解析后才能下载" , Toast.LENGTH_SHORT).show();
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
            cancelDialog();
            if (url.endsWith("html")) {
                for (DownloadDramaBean downloadDramaBean : downloadBean) {
                    if (downloadDramaBean.getTitle().equals(playNumber)) {
                        downloadDramaBean.setYhdmUrl(url);
                        downloadDramaBean.setShouldParse(true);
                        downloadDramaBean.setSelected(false);
                        break;
                    }
                }
                Toast.makeText(getApplicationContext(), playNumber + " -> 下载出错，可能需要解析后才能下载" , Toast.LENGTH_SHORT).show();
                return;
            }
            long taskId;
            String fileSavePath = savePath + playNumber;
            taskId = createDownloadTask(url.endsWith(".m3u8"), url, fileSavePath);
            DatabaseUtil.insertDownload(animeTitle, source, animeListBean.getImg(), sakuraUrl);
            DatabaseUtil.insertDownloadData(animeTitle, source, playNumber, 0, taskId);
            Log.e("Malimali", animeTitle + "," + source + "," + animeListBean.getImg() + "," + sakuraUrl + ","+ playNumber + "," + taskId);
            startDownloadService();
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
//                    .setExtendField(jsonObject.toString())
                    .m3u8VodOption(m3U8VodOption)   // 调整下载模式为m3u8点播
                    .create();
         else
            return Aria.download(this)
                    .load(url)
                    .setFilePath(savePath + ".mp4")
                    .ignoreCheckPermissions()
                    .ignoreFilePathOccupy()
//                    .setExtendField(jsonObject.toString())
                    .create();
    }

    /**
     * 开启下载服务
     */
    private void startDownloadService() {
//        if (!Utils.isServiceRunning(this, "my.project.sakuraproject.services.DownloadService"))
        startService(new Intent(this, DownloadService.class));
        EventBus.getDefault().post(new Refresh(3));
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
        /*if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bottomAppBar.getLayoutParams();
                int navSize = Utils.getNavigationBarHeight(this) - 15;
            int height = navSize > 100 ? navSize + navSize : 132;
            params.height = height;
            Log.e("height", height + "");
            bottomAppBar.setLayoutParams(params);
        }*/
        /*getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);*/
    }

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

    /*@Override
    public void onSystemUiVisibilityChange(int i) {
        new Handler().postDelayed(() ->
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION), 500);

    }*/
}
