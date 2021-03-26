package my.project.sakuraproject.main.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.wuyr.rippleanimation.RippleAnimation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;

import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.WeekAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.VpSwipeRefreshLayout;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.about.AboutActivity;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.animeTopic.AnimeTopicActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.favorite.FavoriteActivity;
import my.project.sakuraproject.main.search.SearchActivity;
import my.project.sakuraproject.main.setting.SettingActivity;
import my.project.sakuraproject.main.tag.TagActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;

public class HomeActivity extends BaseActivity<HomeContract.View, HomePresenter> implements NavigationView.OnNavigationItemSelectedListener, HomeContract.View {
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    VpSwipeRefreshLayout mSwipe;
    private ImageView headerImg;
    private TextView themeView;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    private WeekAdapter adapter;
    private int week;
    private SearchView mSearchView;
    private MenuItem query;
    private SearchView.SearchAutoComplete queryTextView;
    private String[] tabs = Utils.getArray(R.array.week_array);
    private long exitTime = 0;
    private boolean isChangingTheme = false;
    private int[][] states = new int[][]{
            new int[]{-android.R.attr.state_checked},
            new int[]{android.R.attr.state_checked}
    };
    private String[] sourceItems = Utils.getArray(R.array.source);

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter(this);
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
        EventBus.getDefault().register(this);
        initToolbar();
        initDrawer();
        initSwipe();
        initFragment();
    }

    @Override
    protected void initBeforeView() {
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.isImomoe() ? getResources().getString(R.string.imomoe_name) : getResources().getString(R.string.yhdm_name));
        toolbar.setSubtitle(getResources().getString(R.string.app_sub_name));
        toolbar.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (mSwipe.isRefreshing()) {
                application.showSnackbarMsg(toolbar, Utils.getString(R.string.loading_info));
                return;
            }
            setDefaultSource();
        });
        setSupportActionBar(toolbar);
    }

    private void setDefaultSource() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.select_source));
        int selected = Utils.isImomoe() ? 1 : 0;
        builder.setSingleChoiceItems(sourceItems, selected, (dialog, index) -> {
            switch (index) {
                case 0:
                    SharedPreferencesUtils.setParam(this, "isImomoe", false);
                    toolbar.setTitle(getResources().getString(R.string.yhdm_name));
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(this, "isImomoe", true);
                    toolbar.setTitle(getResources().getString(R.string.imomoe_name));
                    break;
            }
            setDomain(index==1);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setDomain(boolean isImomoe) {
        navigationView.getMenu().findItem(R.id.anime_zt).setVisible(isImomoe ? false : true);
        navigationView.getMenu().findItem(R.id.anime_movie).setVisible(isImomoe ? false : true);
        Sakura.setApi();
        viewpager.removeAllViews();
        removeFragmentTransaction();
        mPresenter.loadData(true);
    }

    public void initDrawer() {
        if (gtSdk23()) {
            StatusBarUtil.setColorForDrawerLayout(this, drawer, getColor(R.color.colorPrimary), 0);
            if (!isDarkTheme)
                this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        else
            StatusBarUtil.setColorForDrawerLayout(this, drawer, getResources().getColor(R.color.colorPrimaryDark), 0);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        int[] colors = new int[]{getResources().getColor(R.color.tabTextColor),
                getResources().getColor(R.color.tabSelectedTextColor)
        };
        ColorStateList csl = new ColorStateList(states, colors);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        View view = navigationView.getHeaderView(0);
        themeView = view.findViewById(R.id.theme);
        headerImg = view.findViewById(R.id.header_img);
        setHeaderImg();
        themeView.setOnClickListener(view2 -> {
            if (Utils.isFastClick()) setTheme(isDarkTheme);
        });
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.anime_zt).setVisible(Utils.isImomoe() ? false :true);
        navigationView.getMenu().findItem(R.id.anime_movie).setVisible(Utils.isImomoe() ? false :true);
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            viewpager.removeAllViews();
            removeFragmentTransaction();
            mPresenter.loadData(true);
        });
    }

    public void initFragment() {
        week = Utils.getWeekOfDate(new Date());
        for (String title : tabs) {
            tab.addTab(tab.newTab());
        }
        tab.setupWithViewPager(viewpager);
        //手动 添加标题必须在 setupwidthViewPager后
        for (int i = 0; i < tabs.length; i++) {
            tab.getTabAt(i).setText(tabs[i]);
        }
        tab.getTabAt(week).select();
        tab.setSelectedTabIndicatorColor(getResources().getColor(R.color.pinka200));
        if (Boolean.parseBoolean(SharedPreferencesUtils.getParam(Sakura.getInstance(), "show_x5_info", true).toString()))
            Utils.showX5Info(this);
    }

    private void setHeaderImg() {
        Drawable[] drawables = themeView.getCompoundDrawables();
        Drawable wrapDrawable = DrawableCompat.wrap(drawables[0]);
        themeView.setText(isDarkTheme ? Utils.getString(R.string.set_light_theme) : Utils.getString(R.string.set_dark_theme));
        headerImg.setImageDrawable(isDarkTheme ? getDrawable(R.drawable.night_img) : getDrawable(R.drawable.light_img));
        themeView.setTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
        DrawableCompat.setTint(wrapDrawable, isDarkTheme ?  getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        query = menu.findItem(R.id.search);
        mSearchView = (SearchView) query.getActionView();
        mSearchView.setQueryHint(Utils.getString(R.string.search_hint));
        mSearchView.setMaxWidth(2000);
        queryTextView = mSearchView.findViewById(R.id.search_src_text);
        mSearchView.findViewById(R.id.search_plate).setBackground(null);
        mSearchView.findViewById(R.id.submit_area).setBackground(null);
        queryTextView.setTextColor(getResources().getColor(R.color.text_color_primary));
        queryTextView.setHintTextColor(getResources().getColor(R.color.text_color_primary));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.replaceAll(" ", "").isEmpty()) {
                    Utils.hideKeyboard(mSearchView);
                    mSearchView.clearFocus();
                    startActivity(new Intent(HomeActivity.this, SearchActivity.class).putExtra("title", query));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else if ((System.currentTimeMillis() - exitTime) > 2000) {
            application.showToastMsg(Utils.getString(R.string.exit_app));
            exitTime = System.currentTimeMillis();
        } else {
            application.removeALLActivity();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (!Utils.isFastClick()) return false;
        switch (item.getItemId()) {
            case R.id.find_anime:
                startActivity(new Intent(this, TagActivity.class));
                break;
            case R.id.anime_movie:
                openAnimeListActivity(Utils.getString(R.string.home_movie_title), Sakura.YHDM_MOVIE_API, true);
                break;
            case R.id.anime_zt:
                Bundle bundle = new Bundle();
                bundle.putString("title", Utils.getString(R.string.home_zt_title));
                bundle.putString("url", Sakura.YHDM_ZT_API);
                startActivity(new Intent(this, AnimeTopicActivity.class).putExtras(bundle));
                break;
            case R.id.anime_jcb:
                openAnimeListActivity(Utils.getString(R.string.home_jcb_title), Sakura.JCB_API, false);
                break;
            case R.id.favorite:
                startActivity(new Intent(this, FavoriteActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.setting:
                startActivityForResult(new Intent(this, SettingActivity.class), 0x10);
                break;
        }
        return true;
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
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            mSwipe.setRefreshing(false);
            application.showErrorToastMsg(msg);
            application.error = msg;
            application.week = new JSONObject();
            setWeekAdapter();
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
        runOnUiThread(() -> {
            if (!getSupportFragmentManager().isDestroyed()) {
                mSwipe.setRefreshing(false);
                application.error = "";
                application.week = map.get("week") == null ? new JSONObject() : (JSONObject) map.get("week");
                setWeekAdapter();
            }
        });
    }

    public void setWeekAdapter() {
        adapter = new WeekAdapter(getSupportFragmentManager(), tab.getTabCount());
        try {
            Field field = ViewPager.class.getDeclaredField("mRestoredCurItem");
            field.setAccessible(true);
            field.set(viewpager, week);
        } catch (Exception e) {
            viewpager.setCurrentItem(week);
            e.printStackTrace();
        }
        viewpager.setAdapter(adapter);
        for (int i = 0; i < tabs.length; i++) {
            tab.getTabAt(i).setText(tabs[i]);
        }
    }

    public void removeFragmentTransaction() {
        try {//避免重启太快恢复
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            for (int i = 0; i < 7 ; i++) {
                fragmentTransaction.remove(adapter.getItem(i));
            }
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
        }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtil.closeDB();
        EventBus.getDefault().unregister(this);
    }

/*    @Override
    public void recreate() {
        removeFragmentTransaction();
        super.recreate();
    }*/

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int[] lightColors = new int[]{getResources().getColor(R.color.light_navigation_text_color),
                getResources().getColor(R.color.light_navigation_tini_color)
        };
        int[] darkColors = new int[]{getResources().getColor(R.color.dark_navigation_text_color),
                getResources().getColor(R.color.dark_navigation_tini_color)
        };
        if (isChangingTheme) {
            RippleAnimation.create(themeView).setDuration(1000).start();
            setHeaderImg();
            if (gtSdk23()) {
                if (isDarkTheme) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            /** 设置DrawerLayout相关颜色 **/
            navigationView.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_navigation_color) : getResources().getColor(R.color.light_navigation_color));
            ColorStateList csl = new ColorStateList(states, isDarkTheme ? darkColors : lightColors);
            navigationView.setItemTextColor(csl);
            navigationView.setItemIconTintList(csl);
            drawer.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_navigation_color) : getResources().getColor(R.color.light_navigation_color));
            /** 设置Toolbar相关颜色 **/
            toolbar.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color));
            toolbar.setTitleTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            toolbar.setSubtitleTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            toolbar.getNavigationIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color), BlendModeCompat.SRC_ATOP));
            ImageView searchIcon = mSearchView.findViewById(androidx.appcompat.R.id.search_button);
            searchIcon.setColorFilter(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            ImageView searchIcon2 = mSearchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            searchIcon2.setColorFilter(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            /** 设置TabLayout相关颜色 **/
            int[] colors = new int[]{isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) :getResources().getColor(R.color.dark_toolbar_color) ,
                    getResources().getColor(R.color.tabSelectedTextColor)
            };
            tab.setTabTextColors(new ColorStateList(states, colors));
            tab.setTabTextColors(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) :getResources().getColor(R.color.dark_toolbar_color), getResources().getColor(R.color.tabSelectedTextColor));
            tab.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_navigation_color) : getResources().getColor(R.color.light_navigation_color));
            /** 设置searchView相关颜色 **/
            queryTextView.setTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            queryTextView.setHintTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
            emptyView.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_window_color) : getResources().getColor(R.color.light_window_color));
            if (gtSdk23()) StatusBarUtil.setColorForDrawerLayout(this, drawer, isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color), 0);
            else StatusBarUtil.setColorForDrawerLayout(this, drawer, isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color_lt23), 0);
            if (adapter != null)
                for (int i=0,size=adapter.getCount(); i<size; i++) {
                    WeekFragment weekFragment = (WeekFragment) adapter.getItem(i);
                    if (weekFragment.adapter != null)
                        weekFragment.adapter .notifyDataSetChanged();
                }
            isChangingTheme = false;
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 0) {
            viewpager.removeAllViews();
            removeFragmentTransaction();
            mPresenter.loadData(true);
        }
    }
}
