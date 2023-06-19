package my.project.sakuraproject.main.my;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.MyFragmentAdapter;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.Utils;

/**
 * （旧）我的列表
 * @deprecated 拆分为 {@link FavoriteActivity} {@link HistoryActivity} {@link DownloadActivity}
 */
@Deprecated
public class MyActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    private MyFragmentAdapter myFragmentAdapter;
    private String[] tabTitleArr = Utils.getArray(R.array.my_titles);
    @BindView(R.id.msg)
    CoordinatorLayout msg;
//    private SlidrInterface slidrInterface;

    public static int index = 0;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {}

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_my;
    }

    @Override
    protected void init() {
//        slidrInterface = Slidr.attach(this, Utils.defaultInit());
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) msg.getLayoutParams();
        params.setMargins(10, 0, 10, Utils.getNavigationBarHeight(this) - 5);
        initToolbar();
        initFab();
        initFragment();
    }

    private void initToolbar() {
        toolbar.setTitle(getResources().getString(R.string.my_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initFab() {
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this));
            fab.setLayoutParams(params);
        }
    }

    private void initFragment() {
        tab.addTab(tab.newTab());
        tab.addTab(tab.newTab());
        tab.addTab(tab.newTab());
        tab.setupWithViewPager(viewpager);
        tab.getTabAt(0).select();
        tab.setSelectedTabIndicatorColor(getResources().getColor(R.color.tabSelectedTextColor));
        myFragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager(), tab.getTabCount(), getIntent().getExtras() != null ? (List<AnimeUpdateInfoBean>) getIntent().getExtras().getSerializable("animeUpdateInfoBeans") : null);
        viewpager.setAdapter(myFragmentAdapter);
        tab.getTabAt(0).setText(tabTitleArr[0]);
        tab.getTabAt(1).setText(tabTitleArr[1]);
        tab.getTabAt(2).setText(tabTitleArr[2]);
        viewpager.addOnPageChangeListener(this);
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        enableSliding(position == 0);
        if (position == 2) // 解决Viewpager嵌套Fragment中的recyclerView notifyDataSetChanged无效问题
            myFragmentAdapter.getItem(position).getView().requestLayout();
    }

    @Override
    public void onPageSelected(int position) {
//        enableSliding(position == 0);
        switch (position) {
            case 1:
                if (!fab.isShown()) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setAnimation(AnimationUtils.loadAnimation(this, R.anim.my_fab_in));
                }
                break;
            default:
                fab.setVisibility(View.GONE);
                fab.setAnimation(AnimationUtils.loadAnimation(this, R.anim.my_fab_out));
                break;
        }
    }

    /*private void enableSliding(boolean enable){
        if (Utils.getSlidrConfig()) return;
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }*/

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent() {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void setConfigurationChanged() {

    }
}
