package my.project.sakuraproject.main.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.custom.FixFragmentNavigator;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.home.fragment.HomeFragment;
import my.project.sakuraproject.main.home.fragment.MyFragment;
import my.project.sakuraproject.main.search.SearchActivity;
import my.project.sakuraproject.services.DownloadService;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

public class MainActivity extends BaseActivity {
    @BindView(R.id.nav_view)
    BottomNavigationView bottomNavigationView;
    private long exitTime = 0;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private MenuItem searchView;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        initToolbar();
        initDialog();
        initNavBar();
//        startService(new Intent(this, DownloadService.class));
    }

    private void initToolbar() {
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
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

    private void initNavBar() {
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };
        int[] colors = new int[]{isDarkTheme ? getResources().getColor(R.color.dark_navigation_text_color) : getResources().getColor(R.color.light_navigation_text_color),
                getResources().getColor(R.color.colorAccent)
        };
        ColorStateList csl = new ColorStateList(states, colors);
        bottomNavigationView.setItemActiveIndicatorEnabled(false);
        bottomNavigationView.setItemIconTintList(csl);
        bottomNavigationView.setItemTextColor(csl);
        Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        //fragment的重复加载问题和NavController有关
        NavController navController = NavHostFragment.findNavController(fragmentById);
        NavigatorProvider provider = navController.getNavigatorProvider();
        //设置自定义的navigator
        FixFragmentNavigator fixFragmentNavictor = new FixFragmentNavigator(this, fragmentById.getChildFragmentManager(), fragmentById.getId());
        provider.addNavigator(fixFragmentNavictor);
        NavGraph navDestinations = initNavGraph(provider, fixFragmentNavictor);
        navController.setGraph(navDestinations);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            navController.navigate(item.getItemId());
            return true;
        });
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            toolbar.setNavigationIcon(null);
            switch (destination.getId()) {
                case R.id.home:
                    toolbar.setTitle(getString(R.string.app_name));
                    toolbar.setSubtitle(Utils.isImomoe() ? getResources().getString(R.string.imomoe) : getResources().getString(R.string.yhdm));
                    break;
                case R.id.my:
                    toolbar.setTitle(getString(R.string.my_title));
                    toolbar.setSubtitle("");
                    break;
            }
        });
    }

    private NavGraph initNavGraph(NavigatorProvider provider, FixFragmentNavigator fragmentNavigator) {
        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));
        //用自定义的导航器来创建
        FragmentNavigator.Destination destination1 = fragmentNavigator.createDestination();
        destination1.setId(R.id.home);
        destination1.setClassName(HomeFragment.class.getCanonicalName());
        navGraph.addDestination(destination1);

        FragmentNavigator.Destination destination2 = fragmentNavigator.createDestination();
        destination2.setId(R.id.my);
        destination2.setClassName(MyFragment.class.getCanonicalName());
        navGraph.addDestination(destination2);

        navGraph.setStartDestination(destination1.getId());

        return navGraph;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.search:
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                break;
        }
        return true;
    }

    private void setTheme(boolean isDark) {
        if (isDark) {
            isDarkTheme = false;
            SharedPreferencesUtils.setParam(getApplicationContext(), "darkTheme", false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            isDarkTheme = true;
            SharedPreferencesUtils.setParam(getApplicationContext(), "darkTheme", true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
//        recreate();
        setThemeConfig();
    }

    private void setThemeConfig() {
        if (gtSdk23()) {
            if (isDarkTheme) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        /** 设置Toolbar相关颜色 **/
        toolbar.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color));
        toolbar.setTitleTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
        toolbar.setSubtitleTextColor(isDarkTheme ? getResources().getColor(R.color.light_toolbar_color) : getResources().getColor(R.color.dark_toolbar_color));
        searchView.setIcon(isDarkTheme ? getResources().getDrawable(R.drawable.baseline_search_white_48dp) : getResources().getDrawable(R.drawable.baseline_search_black_48dp));
        setStatusBarColor();
        bottomNavigationView.setBackgroundColor(isDarkTheme ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color));
        bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.bottom_view_color));
        bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.bottom_view_color));
        EventBus.getDefault().post(new Refresh(-2));
        new Handler().postDelayed(() -> {
            recreate();
        },1000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        searchView = menu.findItem(R.id.search);
        searchView.setIcon(isDarkTheme ? getResources().getDrawable(R.drawable.baseline_search_white_48dp) : getResources().getDrawable(R.drawable.baseline_search_black_48dp));
        return true;
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

    @Override
    protected void initBeforeView() {

    }

    @Override
    protected void setConfigurationChanged() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == -1)
            setTheme(isDarkTheme);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        DatabaseUtil.closeDB();
        stopService(new Intent(this, DownloadService.class));
    }
}
