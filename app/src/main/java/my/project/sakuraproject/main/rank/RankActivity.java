package my.project.sakuraproject.main.rank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.RankListAdapter;
import my.project.sakuraproject.bean.SiliSiliRankBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.util.Utils;

public class RankActivity extends BaseActivity<RankContract.View, RankPresenter> implements RankContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.selected_text)
    AutoCompleteTextView selectedView;
    @BindView(R.id.selected_layout)
    TextInputLayout selectedLayout;
    private List<String> topTitles;
    private ArrayAdapter selectedAdapter;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private List<SiliSiliRankBean> siliSiliRankBeans = new ArrayList<>();
    private List<SiliSiliRankBean.RankItem> rankItems = new ArrayList<>();
    private RankListAdapter adapter;
    private int selectedIndex = 0;

    @Override
    protected RankPresenter createPresenter() {
        return new RankPresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_rank;
    }

    @Override
    protected void init() {
        initToolbar();
        initSwipe();
        initAdapter();
    }

    public void initToolbar() {
        toolbar.setTitle("排行榜");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            selectedIndex = 0;
            rankItems.clear();
            adapter.setNewData(rankItems);
            loadData();
        });
    }

    public void initAdapter() {
        adapter = new RankListAdapter(this, rankItems);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            SiliSiliRankBean.RankItem rankItem = (SiliSiliRankBean.RankItem) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("name", rankItem.getTitle());
            String sakuraUrl = rankItem.getUrl();
            bundle.putString("url", sakuraUrl);
            startActivity(new Intent(this, DescActivity.class).putExtras(bundle));
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void initBeforeView() {

    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
        selectedLayout.setVisibility(View.GONE);
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                errorTitle.setText(msg);
                adapter.setEmptyView(errorView);
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        adapter.setEmptyView(emptyView);
    }

    @Override
    public void showLog(String url) {

    }

    @Override
    public void showSuccess(List<SiliSiliRankBean> siliSiliRankBeans) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                this.siliSiliRankBeans = siliSiliRankBeans;
                topTitles = new ArrayList<>();
                for (SiliSiliRankBean siliSiliRankBean : siliSiliRankBeans) {
                    topTitles.add(siliSiliRankBean.getTitle());
                }
                selectedView.setText(topTitles.get(0));
                selectedAdapter = new ArrayAdapter(this, R.layout.list_item, topTitles);
                selectedView.setAdapter(selectedAdapter);
                selectedView.setOnItemClickListener((parent, view, position, id) -> {
                    setAdapterData(position);
                });
                selectedLayout.setVisibility(View.VISIBLE);
                adapter.setNewData(siliSiliRankBeans.get(selectedIndex).getRankItems());
            }
        });
    }

    private void setAdapterData(int position) {
        selectedIndex = position;
        rankItems = siliSiliRankBeans.get(position).getRankItems();
        adapter.setNewData(rankItems);
    }
}
