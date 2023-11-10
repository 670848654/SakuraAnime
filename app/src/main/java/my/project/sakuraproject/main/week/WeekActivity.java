package my.project.sakuraproject.main.week;

import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.WeekFragmentAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.custom.VpSwipeRefreshLayout;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.home.HomeContract;
import my.project.sakuraproject.main.home.HomePresenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

public class WeekActivity extends BaseActivity<HomeContract.View, HomePresenter> implements HomeContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    VpSwipeRefreshLayout mSwipe;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    private WeekFragmentAdapter adapter;
    private int week;
    private String[] tabs = Utils.getArray(R.array.week_array);
    private int[][] states = new int[][]{
            new int[]{-android.R.attr.state_checked},
            new int[]{android.R.attr.state_checked}
    };
//    private SlidrInterface slidrInterface;

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter(true, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_week;
    }

    @Override
    protected void init() {
//        slidrInterface = Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initSwipe();
        initFragment();
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void initToolbar() {
        toolbar.setTitle(getResources().getString(R.string.app_sub_name));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            viewpager.removeAllViews();
            removeFragmentTransaction();
            loadData();
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
            CustomToast.showToast(this, msg, CustomToast.ERROR);
            application.error = msg;
            application.week = new JSONObject();
            setWeekAdapter(week);
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
                setWeekAdapter(week);
            }
        });
    }

    @Override
    public void showHomeLoadSuccess(List<HomeBean> beans) {

    }

    @Override
    public void showUpdateInfoSuccess(List<AnimeUpdateInfoBean> animeUpdateInfoBeans) {

    }

    public void setWeekAdapter(int pos) {
        adapter = new WeekFragmentAdapter(getSupportFragmentManager(), tab.getTabCount());
        try {
            Field field = ViewPager.class.getDeclaredField("mRestoredCurItem");
            field.setAccessible(true);
            field.set(viewpager, week);
        } catch (Exception e) {
            viewpager.setCurrentItem(pos);
            e.printStackTrace();
        }
        viewpager.setAdapter(adapter);
        for (int i = 0; i < tabs.length; i++) {
            tab.getTabAt(i).setText(tabs[i]);
        }
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                enableSliding(position == 0);
            }

            @Override
            public void onPageSelected(int position) {
                week = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /*private void enableSliding(boolean enable){
        if (Utils.getSlidrConfig()) return;
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }*/

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
    protected void setConfigurationChanged() {

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
