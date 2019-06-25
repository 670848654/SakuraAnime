package my.project.sakuraproject.main.desc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import jp.wasabeef.blurry.Blurry;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DescAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.config.AnimeType;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.main.webview.DefaultWebActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class DescActivity extends BaseActivity<DescContract.View, DescPresenter> implements DescContract.View, VideoContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.anime_img)
    ImageView animeImg;
    @BindView(R.id.desc)
    AppCompatTextView desc;
    private DescAdapter adapter;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<MultiItemEntity> multiItemList = new ArrayList<>();
    private List<AnimeDescBean> drama = new ArrayList<>();
    @BindView(R.id.title_img)
    ImageView imageView;
    private String url, diliUrl, dramaUrl;
    private String animeTitle;
    private String witchTitle;
    private ProgressDialog p;
    @BindView(R.id.favorite)
    FloatingActionButton favorite;
    private boolean isFavorite;
    private VideoPresenter videoPresenter;
    private AnimeListBean animeListBean = new AnimeListBean();
    private List<String> animeUrlList = new ArrayList();
    private boolean mIsLoad = false;

    @Override
    protected DescPresenter createPresenter() {
        return new DescPresenter(diliUrl, this);
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
        StatusBarUtil.setColorForSwipeBack(DescActivity.this, getResources().getColor(R.color.night), 0);
        Slidr.attach(this, Utils.defaultInit());
        getBundle();
        initToolbar();
        initFab();
        initSwipe();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            diliUrl = bundle.getString("url");
            animeTitle = bundle.getString("name");
            animeUrlList.add(diliUrl);
        }
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.loading));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initFab() {
        favorite.setOnClickListener(view -> {
            if (Utils.isFastClick()) favoriteAnime();
        });
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            multiItemList.clear();
            adapter.setNewData(multiItemList);
            mPresenter.loadData(true);
        });
        mSwipe.setRefreshing(true);
    }

    @SuppressLint("RestrictedApi")
    public void initAdapter() {
        adapter = new DescAdapter(this, multiItemList);
        adapter.openLoadAnimation();
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            final AnimeDescBean bean = (AnimeDescBean) adapter.getItem(position);
            switch (bean.getType()) {
                case "play":
                    p = Utils.getProDialog(DescActivity.this, R.string.parsing);
                    bean.setSelect(true);
                    Button v = (Button) adapter.getViewByPosition(mRecyclerView, position, R.id.tag_group);
                    v.setBackgroundResource(R.drawable.button_selected);
                    dramaUrl = VideoUtils.getUrl(bean.getUrl());
                    witchTitle = animeTitle + " - " + bean.getTitle();
                    videoPresenter = new VideoPresenter(animeListBean.getTitle(), dramaUrl, DescActivity.this);
                    videoPresenter.loadData(true);
                    break;
                case "multi":
                    animeTitle = bean.getTitle();
                    diliUrl = VideoUtils.getUrl(bean.getUrl());
                    animeUrlList.add(diliUrl);
                    openAnimeDesc();
                    break;
                case "recommend":
                    animeTitle = bean.getTitle();
                    diliUrl = VideoUtils.getUrl(bean.getUrl());
                    animeUrlList.add(diliUrl);
                    openAnimeDesc();
                    break;
            }
        });
        mRecyclerView.setAdapter(adapter);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
    }

    @SuppressLint("RestrictedApi")
    public void openAnimeDesc() {
        toolbar.setTitle(Utils.getString(R.string.loading));
        animeImg.setImageDrawable(getDrawable(R.drawable.loading));
        desc.setText("");
        mSwipe.setRefreshing(true);
        imageView.setImageDrawable(null);
        animeListBean = new AnimeListBean();
        favorite.setVisibility(View.GONE);
        mPresenter = new DescPresenter(diliUrl, this);
        multiItemList.clear();
        adapter.setNewData(multiItemList);
        mPresenter.loadData(true);
    }

    public void goToPlay(List<String> list) {
        new Handler().postDelayed(() -> {
            if (list.size() == 1) oneSource(list);
            else multipleSource(list);
        }, 200);
    }

    /**
     * 只有一个播放地址
     *
     * @param list
     */
    private void oneSource(List<String> list) {
        playAnime(VideoUtils.getVideoUrl(list.get(0)));
    }

    /**
     * 多个播放地址
     *
     * @param list
     */
    private void multipleSource(List<String> list) {
        VideoUtils.showMultipleVideoSources(this,
                list,
                (dialog, index) ->
                        playAnime(VideoUtils.getVideoUrl(list.get(index)))
        );
    }

    private void playAnime(String animeUrl) {
        if (Patterns.WEB_URL.matcher(animeUrl.replace(" ", "")).matches()) {
            if (animeUrl.contains("jx.618g.com")) {
                animeUrl = animeUrl.replaceAll("http://jx.618g.com/\\?url=", "");
                VideoUtils.openWebview(true, this, witchTitle, animeTitle, animeUrl, diliUrl, drama);
            } else if (animeUrl.contains(".mp4") || animeUrl.contains(".m3u8")) {
                switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
                    case 0:
                        //调用播放器
                        VideoUtils.openPlayer(true, this, witchTitle, animeUrl, animeTitle, diliUrl, drama);
                        break;
                    case 1:
                        Utils.selectVideoPlayer(this, animeUrl);
                        break;
                }
            } else {
                Sakura.getInstance().showToastMsg(Utils.getString(R.string.should_be_used_web));
                startActivity(new Intent(DescActivity.this, DefaultWebActivity.class).putExtra("url", animeUrl));
            }
        } else {
            Sakura.getInstance().showToastMsg(Utils.getString(R.string.maybe_can_not_play));
            startActivity(new Intent(DescActivity.this, DefaultWebActivity.class).putExtra("url",String.format(Api.PARSE_API, animeUrl)));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x10 && resultCode == 0x20) {
            mSwipe.setRefreshing(true);
            multiItemList = new ArrayList<>();
            adapter.notifyDataSetChanged();
            mPresenter.loadData(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (animeUrlList.size() == 1) super.onBackPressed();
        else {
            if (!mIsLoad) {
                animeUrlList.remove(animeUrlList.size() - 1);
                diliUrl = animeUrlList.get(animeUrlList.size() - 1);
                openAnimeDesc();
            } else Sakura.getInstance().showToastMsg(Utils.getString(R.string.load_desc_info));
        }
    }

    public void favoriteAnime() {
        setResult(200);
        isFavorite = DatabaseUtil.favorite(animeListBean);
        if (isFavorite) {
            Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_white_48dp).into(favorite);
            Utils.showSnackbar(toolbar, Utils.getString(R.string.join_ok));
        } else {
            Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_border_white_48dp).into(favorite);
            Utils.showSnackbar(toolbar, Utils.getString(R.string.join_error));
        }
    }

    public void setCollapsingToolbar() {
        Glide.with(DescActivity.this).asBitmap().load(animeListBean.getImg()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Blurry.with(DescActivity.this)
                        .radius(4)
                        .sampling(2)
                        .async()
                        .from(resource)
                        .into(imageView);
            }
        });
        toolbar.setTitle(animeListBean.getTitle());
        Utils.setDefaultImage(this, animeListBean.getImg(), animeImg);
        desc.setText(animeListBean.getDesc());
    }

    @Override
    public void showLoadingView() {
        mIsLoad = true;
        showEmptyVIew();
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mIsLoad = false;
                mSwipe.setRefreshing(false);
                setCollapsingToolbar();
                mRecyclerView.setLayoutManager(new LinearLayoutManager(DescActivity.this));
                errorTitle.setText(msg);
                adapter.setEmptyView(errorView);
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        mSwipe.setRefreshing(true);
        adapter.setEmptyView(emptyView);
    }

    @Override
    public void showSuccessMainView(List<MultiItemEntity> list) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mIsLoad = false;
                final GridLayoutManager manager = new GridLayoutManager(DescActivity.this, 15);
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int index = 0;
                        switch (adapter.getItemViewType(position)) {
                            case AnimeType.TYPE_LEVEL_0:
                                index = manager.getSpanCount();
                                break;
                            case AnimeType.TYPE_LEVEL_1:
                                index = 3;
                                break;
                            case AnimeType.TYPE_LEVEL_2:
                                index = 5;
                                break;
                            case AnimeType.TYPE_LEVEL_3:
                                index = 5;
                                break;
                        }
                        return index;
                    }
                });
                // important! setLayoutManager should be called after setAdapter
                mRecyclerView.setLayoutManager(manager);
                multiItemList = list;
                mSwipe.setRefreshing(false);
                setCollapsingToolbar();
                adapter.setNewData(multiItemList);
                adapter.expand(0);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_in_browser:
                Utils.viewInChrome(this, diliUrl);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.desc_menu, menu);
        return true;
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
                    if (isFavorite)
                        Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_white_48dp).into(favorite);
                    else
                        Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_border_white_48dp).into(favorite);
                    favorite.startAnimation(Utils.animationOut(1));
                    favorite.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != videoPresenter)
            videoPresenter.detachView();
    }

    @Override
    public void cancelDialog() {
        Utils.cancelProDialog(p);
    }

    @Override
    public void getVideoSuccess(List<String> list) {
        runOnUiThread(() -> goToPlay(list));
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> VideoUtils.showErrorInfo(DescActivity.this, dramaUrl));
    }

    @Override
    public void getVideoError() {
        runOnUiThread(() -> application.showToastMsg(Utils.getString(R.string.error_700)));
    }

    @Override
    public void showSuccessDramaView(List<AnimeDescBean> list) {
        drama = list;
    }

    @Override
    public void errorDramaView() {

    }
}
