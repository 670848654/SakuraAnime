package my.project.sakuraproject.main.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.HomeAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.HomeHeaderBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.animeTopic.AnimeTopicActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.main.my.MyActivity;
import my.project.sakuraproject.main.search.SearchActivity;
import my.project.sakuraproject.main.setting.SettingActivity;
import my.project.sakuraproject.main.tag.TagActivity;
import my.project.sakuraproject.main.updateList.UpdateListActivity;
import my.project.sakuraproject.main.week.WeekActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

/**
 * （旧）应用首页
 * @deprecated 现使用 {@link MainActivity}
 */
@Deprecated
public class HomeActivity extends BaseActivity<HomeContract.View, HomePresenter> implements HomeContract.View, HomeAdapter.OnItemClick {
    @BindView(R.id.root)
    RelativeLayout root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private MenuItem myView;
    private MenuItem themeView;
    private MenuItem settingView;
    private MenuItem searchView;
    private ImageView themeImageView;
    private long exitTime = 0;
    private boolean isChangingTheme = false;
    private String[] sourceItems = Utils.getArray(R.array.source);
    private List<HomeHeaderBean.HeaderDataBean> headerDataBeans;
    List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private HomeAdapter adapter;
    private List<AnimeUpdateInfoBean> animeUpdateInfoBeans;

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter(false, this);
//        return new HomePresenter(false, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_home;
    }

    @Override
    protected void init() {
        DatabaseUtil.deleteDistinctData(this);
        EventBus.getDefault().register(this);
        initToolbar();
        initDialog();
        initSwipe();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
    }

    public void initToolbar() {
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setSubtitle(Utils.isImomoe() ? getResources().getString(R.string.imomoe) : getResources().getString(R.string.yhdm));
        toolbar.setNavigationIcon(isDarkTheme ? getResources().getDrawable(R.drawable.baseline_sync_alt_white_48dp) : getResources().getDrawable(R.drawable.baseline_sync_alt_black_48dp));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (mSwipe.isRefreshing()) {
//                Sakura.getInstance().showToastMsg(Utils.getString(R.string.loading_info));
                CustomToast.showToast(this, Utils.getString(R.string.loading_info), CustomToast.WARNING);
                return;
            }
            setDefaultSource();
        });
    }

    private void setDefaultSource() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.select_source));
        int selected = Utils.isImomoe() ? 1 : 0;
        builder.setSingleChoiceItems(sourceItems, selected, (dialog, index) -> {
            switch (index) {
                case 0:
                    SharedPreferencesUtils.setParam(this, "isImomoe", false);
                    toolbar.setSubtitle(getResources().getString(R.string.yhdm));
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(this, "isImomoe", true);
                    toolbar.setSubtitle(getResources().getString(R.string.imomoe));
                    break;
            }
            setDomain();
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setDomain() {
        Sakura.setApi();
        multiItemEntities.clear();
        adapter.setNewData(multiItemEntities);
        loadData();
    }

    public void initDialog() {
        if (Boolean.parseBoolean(SharedPreferencesUtils.getParam(Sakura.getInstance(), "show_x5_info", true).toString()))
            Utils.showAlert(this,
                    getString(R.string.x5_info_title),
                    getString(R.string.x5_info),
                    false,
                    getString(R.string.x5_info_positive),
                    null, null, (dialogInterface, i) ->{
                        SharedPreferencesUtils.setParam(this, "show_x5_info", false);
                        dialogInterface.dismiss();
                    } , null, null);
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            loadData();
            multiItemEntities.clear();
            adapter.setNewData(multiItemEntities);
        });
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HomeAdapter(this, multiItemEntities, this);
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (adapter.getItemViewType(position)) {
                case HomeAdapter.TYPE_LEVEL_1:
                    HomeBean homeBean = (HomeBean) adapter.getData().get(position);
                    if (homeBean.getMoreUrl().isEmpty()) return;
                    onMoreClick(homeBean.getTitle(), homeBean.getMoreUrl());
                    break;
            }
        });
        recyclerView.setAdapter(adapter);
        if (Utils.checkHasNavigationBar(this)) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.my:
                Bundle bundle = new Bundle();
                bundle.putSerializable("animeUpdateInfoBeans", (Serializable) animeUpdateInfoBeans);
                startActivity(new Intent(this, MyActivity.class).putExtras(bundle));
                break;
            case R.id.setting:
                startActivityForResult(new Intent(this, SettingActivity.class), 0x10);
                break;
            case R.id.theme:
                setTheme(isDarkTheme);
                break;
            case R.id.search:
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        myView = menu.findItem(R.id.my);
        themeView = menu.findItem(R.id.theme);
        settingView = menu.findItem(R.id.setting);
        searchView = menu.findItem(R.id.search);
        setMenuIconColor();
        return true;
    }

    private void setMenuIconColor() {
        myView.setIcon(isDarkTheme ? getResources().getDrawable(R.drawable.baseline_account_circle_white_48dp) : getResources().getDrawable(R.drawable.baseline_account_circle_black_48dp));
        themeView.setIcon(isDarkTheme ? getResources().getDrawable(R.drawable.baseline_style_white_48dp) : getResources().getDrawable(R.drawable.baseline_style_black_48dp));
        settingView.setIcon(isDarkTheme ? getResources().getDrawable(R.drawable.ic_settings_white_48dp) : getResources().getDrawable(R.drawable.baseline_settings_black_48dp));
        searchView.setIcon(isDarkTheme ? getResources().getDrawable(R.drawable.baseline_search_white_48dp) : getResources().getDrawable(R.drawable.baseline_search_black_48dp));
    }

    @Override
    public void onBackPressed() {
//        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            CustomToast.showToast(this, Utils.getString(R.string.exit_app), CustomToast.DEFAULT);
            exitTime = System.currentTimeMillis();
        } else {
            application.removeALLActivity();
        }
    }

    private void openAnimeListActivity(String title, String url, boolean isMovie) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        bundle.putBoolean("isMovie", isMovie);
        bundle.putBoolean("isImomoe", Utils.isImomoe());
        startActivity(new Intent(this, AnimeListActivity.class).putExtras(bundle));
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x10 && resultCode == 0x20) {
            viewpager.removeAllViews();
            removeFragmentTransaction();
            mPresenter.loadData(true);
        }
    }*/

    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
        application.error = "";
        application.week = new JSONObject();
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            mSwipe.setRefreshing(false);
//            application.showErrorToastMsg(msg);
            CustomToast.showToast(this, msg, CustomToast.ERROR);
            errorTitle.setText(msg);
            adapter.setEmptyView(errorView);
        });
    }

    @Override
    public void showEmptyVIew() {
    }

    @Override
    public void showLog(String url) {
//        runOnUiThread(() -> application.showToastShortMsg(url));
    }

    @Override
    public void showLoadSuccess(LinkedHashMap map) {
    }

    @Override
    public void showHomeLoadSuccess(List<HomeBean> beans) {
        runOnUiThread(() -> {
            mSwipe.setRefreshing(false);
            multiItemEntities = new ArrayList<>();
            headerDataBeans = new ArrayList<>();
            headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("新番时间表", R.drawable.baseline_calendar_month_white_48dp, HomeHeaderBean.TYPE_XFSJB));
            if (!Utils.isImomoe()) {
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("动漫分类", R.drawable.baseline_filter_white_48dp, HomeHeaderBean.TYPE_DMFL));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("动漫电影", R.drawable.baseline_movie_white_48dp, HomeHeaderBean.TYPE_DMDY));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("动漫专题", R.drawable.outline_video_library_white_48dp, HomeHeaderBean.TYPE_DMZT));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("剧场版", R.drawable.ic_ondemand_video_white_48dp, HomeHeaderBean.TYPE_JCB));
            } else {
                /*headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("动漫分类", R.drawable.baseline_filter_white_48dp, HomeHeaderBean.TYPE_DMFL_MALIMALI_TAG));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("日韩动漫", R.drawable.baseline_movie_white_48dp, HomeHeaderBean.TYPE_DMFL_MALIMALI_JAPAN));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("国产动漫", R.drawable.outline_video_library_white_48dp, HomeHeaderBean.TYPE_DMFL_MALIMALI_CHINA));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("欧美动漫", R.drawable.ic_ondemand_video_white_48dp, HomeHeaderBean.TYPE_DMFL_MALIMALI_EUROPE));*/
            }
            multiItemEntities.add(new HomeHeaderBean(headerDataBeans));
            for (HomeBean homeBean : beans) {
                multiItemEntities.add(homeBean);
            }
            adapter.setNewData(multiItemEntities);
        });
    }

    @Override
    public void showUpdateInfoSuccess(List<AnimeUpdateInfoBean> beans) {
        animeUpdateInfoBeans = beans;
    }

    private void setTheme(boolean isDark) {
        isChangingTheme = true;
        if (isDark) {
            isDarkTheme = false;
            SharedPreferencesUtils.setParam(getApplicationContext(), "darkTheme", false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            isDarkTheme = true;
            SharedPreferencesUtils.setParam(getApplicationContext(), "darkTheme", true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        setThemeConfig();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtil.closeDB();
        EventBus.getDefault().unregister(this);
    }

    private void setThemeConfig() {
        /*int[] lightColors = new int[]{getResources().getColor(R.color.light_navigation_text_color),
                getResources().getColor(R.color.light_navigation_tini_color)
        };
        int[] darkColors = new int[]{getResources().getColor(R.color.dark_navigation_text_color),
                getResources().getColor(R.color.dark_navigation_tini_color)
        };*/
        if (isChangingTheme) {
//            RippleAnimation.create(toolbar).setDuration(1000).start();
            if (gtSdk23()) {
                if (isDarkTheme) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            /** 设置DrawerLayout相关颜色 **/
            /*navigationView.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_navigation_color) : getResources().getColor(R.color.light_navigation_color));
            ColorStateList csl = new ColorStateList(states, isDarkTheme ? darkColors : lightColors);
            navigationView.setItemTextColor(csl);
            navigationView.setItemIconTintList(csl);
            drawer.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_navigation_color) : getResources().getColor(R.color.light_navigation_color));*/
            /** 设置Toolbar相关颜色 **/
            toolbar.setNavigationIcon(isDarkTheme ? getResources().getDrawable(R.drawable.baseline_sync_alt_white_48dp) : getResources().getDrawable(R.drawable.baseline_sync_alt_black_48dp));
            toolbar.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color));
            toolbar.setTitleTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            toolbar.setSubtitleTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
//            toolbar.getNavigationIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color), BlendModeCompat.SRC_ATOP));
           /* ImageView searchIcon = mSearchView.findViewById(androidx.appcompat.R.id.search_button);
            searchIcon.setColorFilter(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            ImageView searchIcon2 = mSearchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            searchIcon2.setColorFilter(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));*/
            setMenuIconColor();
            /** 设置searchView相关颜色 **/
//            queryTextView.setTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
//            queryTextView.setHintTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            emptyView.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_window_color) : getResources().getColor(R.color.light_window_color));
            /*if (gtSdk23()) StatusBarUtil.setColorForDrawerLayout(this, drawer, isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color), 0);
            else StatusBarUtil.setColorForDrawerLayout(this, drawer, isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color_lt23), 0);*/
            root.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color));
            setStatusBarColor();
            adapter.setNewData(multiItemEntities);
            isChangingTheme = false;
        }
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 0) {
            multiItemEntities.clear();
            adapter.setNewData(multiItemEntities);
            mPresenter.loadData(true);
        }
    }

    @Override
    public void onHeaderClick(HomeHeaderBean.HeaderDataBean bean) {
        Bundle bundle = new Bundle();
        switch (bean.getType()) {
            case HomeHeaderBean.TYPE_XFSJB:
                startActivity(new Intent(this, WeekActivity.class));
                break;
            case HomeHeaderBean.TYPE_DMFL:
                startActivity(new Intent(this, TagActivity.class));
                break;
            case HomeHeaderBean.TYPE_DMDY:
                openAnimeListActivity(Utils.getString(R.string.home_movie_title), Sakura.MOVIE_API, true);
                break;
            case HomeHeaderBean.TYPE_DMZT:
                bundle.putString("title", Utils.getString(R.string.home_zt_title));
                bundle.putString("url", Sakura.YHDM_ZT_API);
                startActivity(new Intent(this, AnimeTopicActivity.class).putExtras(bundle));
                break;
            case HomeHeaderBean.TYPE_JCB:
                openAnimeListActivity(Utils.getString(R.string.home_jcb_title), Sakura.JCB_API, false);
                break;
            //===========================================================
            /*case HomeHeaderBean.TYPE_DMFL_MALIMALI_TAG:
                bundle.putString("homeParam", Api.MALIMALI_TAG_DEFAULT);
//                bundle.putString("title", "全部类型");
                bundle.putString("title", "全部");
                startActivity(new Intent(this, MaliTagActivity.class).putExtras(bundle));
                break;
            case HomeHeaderBean.TYPE_DMFL_MALIMALI_JAPAN:
                bundle.putString("homeParam", Api.MALIMALI_JAPAN);
                bundle.putString("title", bean.getTitle());
                startActivity(new Intent(this, MaliTagActivity.class).putExtras(bundle));
                break;
            case HomeHeaderBean.TYPE_DMFL_MALIMALI_CHINA:
                bundle.putString("homeParam", Api.MALIMALI_CHINA);
                bundle.putString("title", bean.getTitle());
                startActivity(new Intent(this, MaliTagActivity.class).putExtras(bundle));
                break;
            case HomeHeaderBean.TYPE_DMFL_MALIMALI_EUROPE:
                bundle.putString("homeParam", Api.MALIMALI_EUROPE);
                bundle.putString("title", bean.getTitle());
                startActivity(new Intent(this, MaliTagActivity.class).putExtras(bundle));
                break;*/
        }
    }

    @Override
    public void onAnimeClick(HomeBean.HomeItemBean data) {
        Bundle bundle = new Bundle();
        bundle.putString("name", data.getTitle());
        String sakuraUrl = data.getUrl();
        bundle.putString("url", sakuraUrl);
        startActivity(new Intent(this, DescActivity.class).putExtras(bundle));
    }

    public void onMoreClick(String title, String url) {
        if (url.contains("new.html"))
            openUpdateList(title, url, true);
        else if (url.contains("new"))
            openUpdateList(title, url, false);
        else if (url.contains("list") || url.contains("movie"))
            openAnimeListActivity(Utils.getString(R.string.home_movie_title), Sakura.MOVIE_API, true);
        else
            openTagList(title, url);
    }

    private void openUpdateList(String title, String url, boolean isImomoe) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        bundle.putBoolean("isImomoe", isImomoe);
        startActivity(new Intent(this, UpdateListActivity.class).putExtras(bundle));
    }

    private void openTagList(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        startActivity(new Intent(this, TagActivity.class).putExtras(bundle));
    }
}
