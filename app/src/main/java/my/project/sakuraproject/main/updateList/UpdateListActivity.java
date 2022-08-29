package my.project.sakuraproject.main.updateList;

import android.content.Intent;
import android.os.Bundle;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.UpdateListAdapter;
import my.project.sakuraproject.bean.AnimeUpdateBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.util.Utils;

public class UpdateListActivity extends BaseActivity<UpdateListContract.View, UpdateListPresenter> implements UpdateListContract.View {
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private UpdateListAdapter adapter;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<AnimeUpdateBean> list = new ArrayList<>();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private String title, url;
    private boolean isImomoe;

    @Override
    protected UpdateListPresenter createPresenter() {
        return new UpdateListPresenter(url, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(isImomoe);
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
            isImomoe = bundle.getBoolean("isImomoe");
        }
    }

    public void initToolbar() {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            list.clear();
            adapter.setNewData(list);
            mPresenter = createPresenter();
            mPresenter.loadData(isImomoe);
        });
    }

    public void initAdapter() {
        adapter = new UpdateListAdapter(this, list);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            AnimeUpdateBean bean = (AnimeUpdateBean) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("name", bean.getTitle());
            String sakuraUrl = bean.getUrl();
            bundle.putString("url", sakuraUrl);
            startActivity(new Intent(UpdateListActivity.this, DescActivity.class).putExtras(bundle));
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        mRecyclerView.setAdapter(adapter);
        setRecyclerViewView();
    }

    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
    }

    @Override
    public void showSuccessView(List<AnimeUpdateBean> animeList) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                list = animeList;
                adapter.setNewData(list);
            }
        });
    }

    @Override
    public void showErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                errorTitle.setText(msg);
                adapter.setEmptyView(errorView);
            }
        });
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
        if (Utils.isPad()) setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewView() {
        /*String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (list.size() == 0) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            return;
        }
        if (!Utils.isPad()) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
        else {
            if (isInMagicWindow) {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
            }
        }*/
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
