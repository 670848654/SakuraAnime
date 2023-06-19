package my.project.sakuraproject.main.animeTopic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.AnimeListAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.search.SearchActivity;
import my.project.sakuraproject.util.Utils;

public class AnimeTopicActivity extends BaseActivity<AnimeTopicContract.View, AnimeTopicPresenter> implements AnimeTopicContract.View {
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private AnimeListAdapter adapter;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<AnimeListBean> list = new ArrayList<>();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.query)
    FloatingActionButton query;
    private String title, url;
    private int nowPage = 1;
    private int pageCount = 1;
    private boolean isErr = true;

    @Override
    protected AnimeTopicPresenter createPresenter() {
        return new AnimeTopicPresenter(Sakura.DOMAIN + url, nowPage, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_anime;
    }

    @Override
    protected void init() {
//        Slidr.attach(this, Utils.defaultInit());
        getBundle();
        initToolbar();
        initFab();
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
            title = bundle.getString("title");
            url = bundle.getString("url");
        }
    }

    public void initToolbar() {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    @SuppressLint("RestrictedApi")
    public void initFab() {
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) query.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 15);
            query.setLayoutParams(params);
        }
        query.setVisibility(View.VISIBLE);
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            list.clear();
            adapter.setNewData(list);
            nowPage = 1;
            pageCount = 1;
            mPresenter = createPresenter();
            mPresenter.loadData(true);
        });
    }

    public void initAdapter() {
        adapter = new AnimeListAdapter(this, list, true);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            final AnimeListBean bean = (AnimeListBean) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("title", bean.getTitle());
            bundle.putString("url", bean.getUrl());
            bundle.putBoolean("isMovie", false);
            bundle.putBoolean("isImomoe", Utils.isImomoe());
            bundle.putBoolean("isToptic", true);
            startActivity(new Intent(this, AnimeListActivity.class).putExtras(bundle));
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        adapter.setLoadMoreView(new CustomLoadMoreView());
        adapter.setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (nowPage >= pageCount) {
                //数据全部加载完毕
                adapter.loadMoreEnd();
//                application.showSuccessToastMsg(Utils.getString(R.string.no_more));
                CustomToast.showToast(this, Utils.getString(R.string.no_more), CustomToast.SUCCESS);
            } else {
                if (isErr) {
                    //成功获取更多数据
                    nowPage++;
                    mPresenter = createPresenter();
                    mPresenter.loadData(false);
                } else {
                    //获取更多数据失败
                    isErr = true;
                    adapter.loadMoreFail();
                }
            }
        }, 500), mRecyclerView);
        mRecyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.query)
    public void query() {
        startActivity(new Intent(this, SearchActivity.class));
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.loadMoreComplete();
    }

    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
    }

    @Override
    public void showSuccessView(boolean isMain, List<AnimeListBean> animeList) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (isMain) {
                    setRecyclerViewView();
                    mSwipe.setRefreshing(false);
                    list = animeList;
                    adapter.setNewData(list);
                } else {
                    adapter.addData(animeList);
                    setLoadState(true);
                }
            }
        });
    }

    @Override
    public void showErrorView(boolean isMain, String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (isMain) {
                    mSwipe.setRefreshing(false);
                    setRecyclerViewEmpty();
                    errorTitle.setText(msg);
                    adapter.setEmptyView(errorView);
                } else {
                    setLoadState(false);
//                    application.showErrorToastMsg(msg);
                    CustomToast.showToast(this, msg, CustomToast.ERROR);
                }
            }
        });
    }

    @Override
    public void getPageCountSuccessView(int count) {
        pageCount = count;
    }

    @Override
    public void showLoadErrorView(String msg) {
    }

    @Override
    public void showEmptyVIew() {
        adapter.setEmptyView(emptyView);
    }

    @Override
    public void showLog(String url) {
//        runOnUiThread(() -> application.showToastShortMsg(url));
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad())
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 2 : 4));
        else {
            if (isInMagicWindow)
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            else
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 3 : 4));
        }
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }
}
