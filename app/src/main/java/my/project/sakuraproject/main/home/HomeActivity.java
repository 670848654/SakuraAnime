package my.project.sakuraproject.main.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;

import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.WeekAdapter;
import my.project.sakuraproject.application.Sakura;
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
//    private ImageView imageView;
    LinearLayout setTheme;
    private ImageView theme;
    private TextView themeTitle;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    private WeekAdapter adapter;
    private int week;
    private SearchView mSearchView;
    private String[] tabs = Utils.getArray(R.array.week_array);
    private long exitTime = 0;

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
        initToolbar();
        initDrawer();
        initSwipe();
        initFragment();
    }

    @Override
    protected void initBeforeView() {
    }

    public void initToolbar() {
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setSubtitle(getResources().getString(R.string.app_sub_name));
        setSupportActionBar(toolbar);
    }

    public void initDrawer() {
        if (gtSdk23()) {
            StatusBarUtil.setColorForDrawerLayout(this, drawer, getColor(R.color.colorPrimary), 0);
            if (!(Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false))
                this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        else
            StatusBarUtil.setColorForDrawerLayout(this, drawer, getResources().getColor(R.color.colorPrimaryDark), 0);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };
        int[] colors = new int[]{getResources().getColor(R.color.tabTextColor),
                getResources().getColor(R.color.tabSelectedTextColor)
        };
        ColorStateList csl = new ColorStateList(states, colors);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        View view = navigationView.getHeaderView(0);
//        imageView = view.findViewById(R.id.imageView);
//        imageView.setOnClickListener(view1 -> {
//            final ObjectAnimator animator = Utils.tada(imageView);
//            animator.setRepeatCount(0);
//            animator.setDuration(1000);
//            animator.start();
//        });
        setTheme = view.findViewById(R.id.set_theme);
        setTheme.setOnClickListener(view2 -> {
            if ((Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false))
            {
                SharedPreferencesUtils.setParam(getApplicationContext(),"darkTheme",false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else
            {
                SharedPreferencesUtils.setParam(getApplicationContext(),"darkTheme",true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            drawer.closeDrawer(GravityCompat.START);
            HomeActivity.this.getWindow().setWindowAnimations(R.style.WindowAnimationFadeInOut);
            new Handler().postDelayed(this::recreate, 500);
        });
        theme = view.findViewById(R.id.theme);
        themeTitle = view.findViewById(R.id.theme_title);
        if ((Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false)) {
            theme.setImageDrawable(getDrawable(R.drawable.ic_night));
            themeTitle.setText(Utils.getString(R.string.dark));
        } else {
            theme.setImageDrawable(getDrawable(R.drawable.ic_sun));
            themeTitle.setText(Utils.getString(R.string.light));
        }
//        navigationView.getBackground().mutate().setAlpha(150);//0~255透明度值
        navigationView.setNavigationItemSelectedListener(this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        final MenuItem item = menu.findItem(R.id.search);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setQueryHint(Utils.getString(R.string.search_hint));
        mSearchView.setMaxWidth(2000);
        SearchView.SearchAutoComplete textView = mSearchView.findViewById(R.id.search_src_text);
        mSearchView.findViewById(R.id.search_plate).setBackground(null);
        mSearchView.findViewById(R.id.submit_area).setBackground(null);
        textView.setTextColor(getResources().getColor(R.color.text_color_primary));
        textView.setHintTextColor(getResources().getColor(R.color.text_color_primary));
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
                openAnimeListActivity(Utils.getString(R.string.home_movie_title), Sakura.MOVIE_API, true);
                break;
            case R.id.anime_zt:
                Bundle bundle = new Bundle();
                bundle.putString("title", Utils.getString(R.string.home_zt_title));
                bundle.putString("url", Sakura.ZT_API);
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
        startActivity(new Intent(this, AnimeListActivity.class).putExtras(bundle));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x10 && resultCode == 0x20) {
            viewpager.removeAllViews();
            removeFragmentTransaction();
            mPresenter.loadData(true);
        }
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtil.closeDB();
    }

    @Override
    public void recreate() {
        removeFragmentTransaction();
        super.recreate();
    }
}
