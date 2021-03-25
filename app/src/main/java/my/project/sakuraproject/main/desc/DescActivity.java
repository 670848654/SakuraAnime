package my.project.sakuraproject.main.desc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.AnimeDescDetailsAdapter;
import my.project.sakuraproject.adapter.AnimeDescDramaAdapter;
import my.project.sakuraproject.adapter.AnimeDescRecommendAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.custom.MyTextView;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class DescActivity extends BaseActivity<DescContract.View, DescPresenter> implements DescContract.View, VideoContract.View {
//    @BindView(R.id.toolbar)
//    Toolbar toolbar;
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
    private boolean mIsLoad = false;
    @BindView(R.id.details_list)
    RecyclerView detailsRv;
    @BindView(R.id.multi_list)
    RecyclerView multiRv;
    @BindView(R.id.recommend_list)
    RecyclerView recommendRv;
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
    TextView favorite;
    @BindView(R.id.tag_view)
    TagContainerLayout tagContainerLayout;
    @BindView(R.id.update_time)
    MyTextView update_time;
    @BindView(R.id.score_view)
    AppCompatTextView score_view;
/*    @BindView(R.id.inside_view)
    InsideScrollView scrollView;*/
    @BindView(R.id.scrollview)
    NestedScrollView scrollView;
    @BindView(R.id.spinner)
    AppCompatSpinner spinner;
    private ArrayAdapter<String> spinnerAdapter;
    private boolean isImomoe;
    private List<List<ImomoeVideoUrlBean>> imomoeBeans = new ArrayList<>();
    private int nowSource = 0; // 当前源
    private int clickIndex; // 当前点击剧集

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
        StatusBarUtil.setColorForSwipeBack(this, getResources().getColor(R.color.colorPrimaryDark), 0);
//        StatusBarUtil.setTranslucentForImageView(this, 0, toolbar);
        if (isDarkTheme) bg.setVisibility(View.GONE);
        Slidr.attach(this, Utils.defaultInit());
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) msg.getLayoutParams();
        params.setMargins(10, 0, 10, 0);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> mSwipe.setEnabled(scrollView.getScrollY() == 0));
        desc.setNeedExpend(true);
        getBundle();
        initToolbar();
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

    public void initToolbar() {
/*        toolbar.setTitle(Utils.getString(R.string.loading));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());*/
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
        detailsRv.setLayoutManager(getLinearLayoutManager());
        detailsRv.setAdapter(animeDescDetailsAdapter);
        detailsRv.setNestedScrollingEnabled(false);

        animeDescMultiAdapter = new AnimeDescRecommendAdapter(this, animeDescListBean.getAnimeDescMultiBeans());
        animeDescMultiAdapter.setOnItemClickListener((adapter, view, position) -> {
            AnimeDescRecommendBean bean = (AnimeDescRecommendBean) adapter.getItem(position);
            animeTitle = bean.getTitle();
            sakuraUrl = bean.getUrl();
            animeUrlList.add(sakuraUrl);
            openAnimeDesc();
        });
        multiRv.setLayoutManager(getLinearLayoutManager());
        multiRv.setAdapter(animeDescMultiAdapter);
        multiRv.setNestedScrollingEnabled(false);

        animeDescRecommendAdapter = new AnimeDescRecommendAdapter(this, animeDescListBean.getAnimeDescRecommendBeans());
        animeDescRecommendAdapter.setOnItemClickListener((adapter, view, position) -> {
            AnimeDescRecommendBean bean = (AnimeDescRecommendBean) adapter.getItem(position);
            animeTitle = bean.getTitle();
            sakuraUrl = bean.getUrl();
            animeUrlList.add(sakuraUrl);
            openAnimeDesc();
        });
        recommendRv.setLayoutManager(getLinearLayoutManager());
        if (Utils.checkHasNavigationBar(this)) recommendRv.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        recommendRv.setAdapter(animeDescRecommendAdapter);
        recommendRv.setNestedScrollingEnabled(false);

        View dramaView = LayoutInflater.from(this).inflate(R.layout.dialog_drama, null);
        lineRecyclerView = dramaView.findViewById(R.id.drama_list);
        lineRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        animeDescDramaAdapter = new AnimeDescDramaAdapter(this, animeDescListBean.getAnimeDescDetailsBeans());
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
    }

    private LinearLayoutManager getLinearLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        return linearLayoutManager;
    }

    private void initTagClick() {
        tagContainerLayout.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                Bundle bundle = new Bundle();
                bundle.putString("title", animeListBean.getTagTitles().get(position));
                bundle.putString("url", sakuraUrl.contains("/view/") ? animeListBean.getTagUrls().get(position) + "&page=1&" : animeListBean.getTagUrls().get(position));
                bundle.putBoolean("isMovie", animeListBean.getTagUrls().get(position).contains("movie") ? true : false);
                bundle.putBoolean("isImomoe", sakuraUrl.contains("/view/"));
                startActivity(new Intent(DescActivity.this, AnimeListActivity.class).putExtras(bundle));
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

    @SuppressLint("RestrictedApi")
    public void openAnimeDesc() {
//        toolbar.setTitle(Utils.getString(R.string.loading));
        animeImg.setImageDrawable(getDrawable(R.drawable.loading));
        tagContainerLayout.setVisibility(View.GONE);
        tagContainerLayout.setTags("");
        update_time.setText("");
        score_view.setVisibility(View.GONE);
        setTextviewEmpty(desc);
        animeDescListBean = new AnimeDescListBean();
        favorite.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
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
        if (isImomoe) {
            animeDescListBean.getMultipleAnimeDescDetailsBeans().get(nowSource).get(clickIndex).setSelected(true);
            dramaUrl = animeDescListBean.getMultipleAnimeDescDetailsBeans().get(nowSource).get(clickIndex).getUrl();
            witchTitle = animeTitle + " - " + animeDescListBean.getMultipleAnimeDescDetailsBeans().get(nowSource).get(clickIndex).getTitle();
        } else {
            animeDescListBean.getAnimeDescDetailsBeans().get(position).setSelected(true);
            dramaUrl = animeDescListBean.getAnimeDescDetailsBeans().get(position).getUrl();
            witchTitle = animeTitle + " - " + animeDescListBean.getAnimeDescDetailsBeans().get(position).getTitle();
        }
        animeDescDetailsAdapter.notifyDataSetChanged();
        animeDescDramaAdapter.notifyDataSetChanged();
        if (isImomoe && imomoeBeans.size() > 0) {
            String fid = DatabaseUtil.getAnimeID(animeTitle);
            DatabaseUtil.addIndex(fid, Sakura.DOMAIN + animeDescListBean.getMultipleAnimeDescDetailsBeans().get(nowSource).get(clickIndex).getUrl());
            playAnime(imomoeBeans.get(nowSource).get(clickIndex).getVidOrUrl());
            System.out.println(Sakura.DOMAIN + animeDescListBean.getMultipleAnimeDescDetailsBeans().get(nowSource).get(clickIndex).getUrl());
        }
        else {
            videoPresenter = new VideoPresenter(animeTitle, dramaUrl, DescActivity.this);
            videoPresenter.loadData(true);
        }
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
                if (isImomoe)
                    VideoUtils.openImomoePlayer(true, this, witchTitle, animeUrl, animeTitle, sakuraUrl, animeDescListBean.getMultipleAnimeDescDetailsBeans(), imomoeBeans, nowSource);
                else
                    VideoUtils.openPlayer(true, this, witchTitle, animeUrl, animeTitle, sakuraUrl, animeDescListBean.getAnimeDescDetailsBeans());
                break;
            case 1:
                Utils.selectVideoPlayer(this, animeUrl);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x10 && resultCode == 0x20) {
            mPresenter.loadData(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (animeUrlList.size() == 1) super.onBackPressed();
        else {
            if (!mIsLoad) {
                animeUrlList.remove(animeUrlList.size() - 1);
                sakuraUrl = animeUrlList.get(animeUrlList.size() - 1);
                openAnimeDesc();
            } else Sakura.getInstance().showToastMsg(Utils.getString(R.string.load_desc_info));
        }
    }

    public void favoriteAnime() {
        setResult(200);
        isFavorite = DatabaseUtil.favorite(animeListBean);
        Drawable drawable = ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_star_white_48dp : R.drawable.baseline_star_border_white_48dp);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        if (isFavorite) {
            favorite.setCompoundDrawables(drawable,null,null,null);
            favorite.setText(Utils.getString(R.string.has_favorite));
            application.showSnackbarMsg(msg, Utils.getString(R.string.join_ok));
        } else {
            favorite.setCompoundDrawables(drawable,null,null,null);
            favorite.setText(Utils.getString(R.string.favorite));
            application.showSnackbarMsg(msg, Utils.getString(R.string.join_error));
        }
    }

    public void setCollapsingToolbar() {
        GlideUrl imgUrl;
        if (!isImomoe)
            imgUrl = new GlideUrl(Utils.getImgUrl(animeListBean.getImg()), new LazyHeaders.Builder()
                .addHeader("Referer", Sakura.DOMAIN + "/")
                .build());
        else
            imgUrl = new GlideUrl(Utils.getImgUrl(animeListBean.getImg()));
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(300).setCrossFadeEnabled(true).build();
/*        Glide.with(this).load(imgUrl)
                .apply(RequestOptions.bitmapTransform( new BlurTransformation(25, 3)))
                .transition(DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory))
                .into(bg);*/
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error);
        Glide.with(this)
                .load(imgUrl)
                .transition(DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory))
                .apply(options)
                .into(bg);
        Utils.setDefaultImage(this, animeListBean.getUrl(), animeListBean.getImg(), animeImg, false, null, null);
//        toolbar.setTitle(animeListBean.getTitle());
        title.setText(animeListBean.getTitle());
        if (animeListBean.getTagTitles() != null) {
            tagContainerLayout.setTags(animeListBean.getTagTitles());
            tagContainerLayout.setVisibility(View.VISIBLE);
        }else
            tagContainerLayout.setVisibility(View.GONE);
        desc.setContent(animeListBean.getDesc());
        update_time.setText(animeListBean.getUpdateTime());
        if (!isImomoe) {
            score_view.setText(animeListBean.getScore()+"分");
            score_view.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.favorite)
    public void setFavorite() {
        favoriteAnime();
    }

    @OnClick(R.id.exit)
    public void exit() {
        finish();
    }
   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_in_browser:
                if (Utils.isFastClick()) Utils.viewInChrome(this, sakuraUrl);
                break;
            case R.id.favorite:
                if (Utils.isFastClick()) favoriteAnime();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.desc_menu, menu);
        favorite = menu.findItem(R.id.favorite);
        return true;
    }*/

    @Override
    public void showLoadingView() {
        mIsLoad = true;
        showEmptyVIew();
        detailsRv.scrollToPosition(0);
        multiRv.scrollToPosition(0);
        recommendRv.scrollToPosition(0);
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mIsLoad = false;
                mSwipe.setRefreshing(false);
                desc_view.setVisibility(View.GONE);
                playLinearLayout.setVisibility(View.GONE);
                multiLinearLayout.setVisibility(View.GONE);
                recommendLinearLayout.setVisibility(View.GONE);
                error_msg.setText(msg);
                errorBg.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        mSwipe.setRefreshing(true);
        if ( favorite.isShown())
            favorite.setVisibility(View.GONE);
        desc_view.setVisibility(View.GONE);
        playLinearLayout.setVisibility(View.GONE);
        multiLinearLayout.setVisibility(View.GONE);
        recommendLinearLayout.setVisibility(View.GONE);
        errorBg.setVisibility(View.GONE);
    }

    @Override
    public void showLog(String url) {
//        runOnUiThread(() -> application.showToastShortMsg(url));
    }

    @Override
    public void showSuccessMainView(AnimeDescListBean bean) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mIsLoad = false;
                setCollapsingToolbar();
                mSwipe.setRefreshing(false);
                this.animeDescListBean = bean;
                if (animeDescListBean.getMultipleAnimeDescDetailsBeans() != null) {
                    animeDescDetailsAdapter.setNewData(animeDescListBean.getMultipleAnimeDescDetailsBeans().get(0));
                    setAnimeDescDramaAdapter(true, 0);
                    nowSource = 0;
                    // imomoe
                    if (bean.getMultipleAnimeDescDetailsBeans().size() > 1)
                        getSpinner(animeDescListBean.getMultipleAnimeDescDetailsBeans().size());
                } else {
                    animeDescDetailsAdapter.setNewData(bean.getAnimeDescDetailsBeans());
                    spinner.setVisibility(View.GONE);
                }
                if (bean.getAnimeDescMultiBeans().size() > 0)
                    multiLinearLayout.setVisibility(View.VISIBLE);
                else
                    multiLinearLayout.setVisibility(View.GONE);
                animeDescMultiAdapter.setNewData(bean.getAnimeDescMultiBeans());
                animeDescRecommendAdapter.setNewData(bean.getAnimeDescRecommendBeans());
                setAnimeDescDramaAdapter(false, 0);
                desc_view.setVisibility(View.VISIBLE);
                playLinearLayout.setVisibility(View.VISIBLE);
                recommendLinearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setAnimeDescDramaAdapter(boolean isImomoe, int sourceIndex) {
        if (isImomoe) {
            List<AnimeDescDetailsBean> animeDescDetailsBean = animeDescListBean.getMultipleAnimeDescDetailsBeans().get(sourceIndex);
            if (animeDescDetailsBean.size() > 4)
                openDrama.setVisibility(View.VISIBLE);
            else
                openDrama.setVisibility(View.GONE);
            animeDescDramaAdapter.setNewData(animeDescDetailsBean);
        } else {
            if (animeDescListBean.getAnimeDescDetailsBeans().size() > 4)
                openDrama.setVisibility(View.VISIBLE);
            else
                openDrama.setVisibility(View.GONE);
            animeDescDramaAdapter.setNewData(animeDescListBean.getAnimeDescDetailsBeans());
        }
    }

    private void getSpinner(int size) {
        List<String> items = new ArrayList<>();
        for (int i=1; i<size+1; i++) {
            items.add("播放源 " + i);
        }
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                animeDescDetailsAdapter.setNewData(animeDescListBean.getMultipleAnimeDescDetailsBeans().get(position));
                setAnimeDescDramaAdapter(true, position);
                nowSource = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setVisibility(View.VISIBLE);
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
                    Drawable drawable = ContextCompat.getDrawable(this, isFavorite ? R.drawable.baseline_star_white_48dp : R.drawable.baseline_star_border_white_48dp);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    if (isFavorite) {
//                        favorite.setIcon(R.drawable.baseline_star_white_48dp);
                        favorite.setText(Utils.getString(R.string.has_favorite));
                        favorite.setCompoundDrawables(drawable,null,null,null);
                    } else {
//                        favorite.setIcon(R.drawable.baseline_star_border_white_48dp);
                        favorite.setText(Utils.getString(R.string.favorite));
                        favorite.setCompoundDrawables(drawable,null,null,null);
                    }
                    favorite.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void showEmptyDram(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mIsLoad = false;
                mSwipe.setRefreshing(false);
                setCollapsingToolbar();
                desc_view.setVisibility(View.VISIBLE);
                playLinearLayout.setVisibility(View.GONE);
                multiLinearLayout.setVisibility(View.GONE);
                recommendLinearLayout.setVisibility(View.GONE);
                error_msg.setText(msg);
                errorBg.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void isImomoe(boolean isImomoe) {
        this.isImomoe = isImomoe;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != videoPresenter)
            videoPresenter.detachView();
    }

    @Override
    public void cancelDialog() {
        Utils.cancelDialog(alertDialog);
    }

    @Override
    public void getVideoSuccess(List<String> list) {
        runOnUiThread(() -> {
            if (list.size() == 1)
                playAnime(list.get(0));
            else
                VideoUtils.showMultipleVideoSources(this,
                        list,
                        (dialog, index) -> playAnime(list.get(index)), (dialog, which) -> {
                            cancelDialog();
                            dialog.dismiss();
                        }, 1);
        });
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> {
            application.showToastMsg(Utils.getString(R.string.open_web_view));
            VideoUtils.openDefaultWebview(this, dramaUrl.contains("/view/") ? BaseModel.getDomain(true) + dramaUrl : BaseModel.getDomain(false) + dramaUrl);
        });
    }

    @Override
    public void getVideoError() {
        runOnUiThread(() -> application.showErrorToastMsg(Utils.getString(R.string.error_700)));
    }

    @Override
    public void showSuccessDramaView(List<AnimeDescDetailsBean> list) {

    }

    @Override
    public void errorDramaView() {

    }

    @Override
    public void showSuccessImomoeDramaView(List<List<ImomoeVideoUrlBean>> bean) {
        imomoeBeans = bean;
        runOnUiThread(() -> {
            if (imomoeBeans.size() > 0) {
                ImomoeVideoUrlBean imomoeVideoUrlBean = imomoeBeans.get(nowSource).get(clickIndex);
                playAnime(imomoeVideoUrlBean.getVidOrUrl());
            }
        });
    }
}
