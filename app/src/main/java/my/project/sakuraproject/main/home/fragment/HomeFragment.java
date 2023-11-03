package my.project.sakuraproject.main.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.button.MaterialButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.HomeAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.HomeHeaderBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.main.animeList.AnimeListActivity;
import my.project.sakuraproject.main.animeTopic.AnimeTopicActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.main.home.HomeContract;
import my.project.sakuraproject.main.home.HomePresenter;
import my.project.sakuraproject.main.rank.RankActivity;
import my.project.sakuraproject.main.tag.TagActivity;
import my.project.sakuraproject.main.updateList.UpdateListActivity;
import my.project.sakuraproject.main.week.WeekActivity;
import my.project.sakuraproject.util.Utils;

public class HomeFragment extends BaseFragment<HomeContract.View, HomePresenter> implements HomeContract.View, HomeAdapter.OnItemClick {
    private View view;
    @BindView(R.id.root)
    RelativeLayout rootView;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<HomeHeaderBean.HeaderDataBean> headerDataBeans;
    List<MultiItemEntity> multiItemEntities = new ArrayList<>();
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private HomeAdapter adapter;
    @BindView(R.id.ref)
    MaterialButton ref;

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        initSwipe();
        initAdapter();
        return view;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new HomeAdapter(getActivity(), multiItemEntities, this);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (adapter.getItemViewType(position)) {
                case HomeAdapter.TYPE_LEVEL_2:
                    HomeBean homeBean = (HomeBean) adapter.getData().get(position);
                    if (homeBean.getMoreUrl().isEmpty()) return;
                    onMoreClick(homeBean.getTitle(), homeBean.getMoreUrl());
                    break;
            }
        });
        recyclerView.setAdapter(adapter);
//        if (Utils.checkHasNavigationBar(getActivity())) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
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
        startActivity(new Intent(getActivity(), UpdateListActivity.class).putExtras(bundle));
    }

    private void openTagList(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        startActivity(new Intent(getActivity(), TagActivity.class).putExtras(bundle));
    }

    private void openAnimeListActivity(String title, String url, boolean isMovie) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        bundle.putBoolean("isMovie", isMovie);
        bundle.putBoolean("isImomoe", Utils.isImomoe());
        startActivity(new Intent(getActivity(), AnimeListActivity.class).putExtras(bundle));
    }

    private void openSiliTagList(String title, String tagUrl, String[] siliParams) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("tagUrl", tagUrl);
        bundle.putStringArray("siliParams", siliParams);
        startActivity(new Intent(getActivity(), TagActivity.class).putExtras(bundle));
    }

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter(false, this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        switch (refresh.getIndex()) {
            case -2:
                rootView.setBackgroundColor(Utils.getTheme() ? getResources().getColor(R.color.dark_toolbar_color) : getResources().getColor(R.color.light_toolbar_color));
                adapter.notifyDataSetChanged();
                break;
            case 0:
                multiItemEntities.clear();
                adapter.setNewData(multiItemEntities);
                mPresenter.loadData(true);
                break;
        }
    }

    @Override
    public void onHeaderClick(HomeHeaderBean.HeaderDataBean bean) {
        Bundle bundle = new Bundle();
        String tagUrl = "/%s/%s/%s";
        String siliArrStr = "/%s/";
        String[] siliParams = new String[3];
        siliParams[0] = "/vodshow";
        siliParams[1] = "/id";
        switch (bean.getType()) {
            case HomeHeaderBean.TYPE_XFSJB:
                startActivity(new Intent(getActivity(), WeekActivity.class));
                break;
            case HomeHeaderBean.TYPE_DMFL:
                startActivity(new Intent(getActivity(), TagActivity.class));
                break;
            case HomeHeaderBean.TYPE_DMDY:
                openAnimeListActivity(Utils.getString(R.string.home_movie_title), Sakura.MOVIE_API, true);
                break;
            case HomeHeaderBean.TYPE_DMZT:
                bundle.putString("title", Utils.getString(R.string.home_zt_title));
                bundle.putString("url", Sakura.YHDM_ZT_API);
                startActivity(new Intent(getActivity(), AnimeTopicActivity.class).putExtras(bundle));
                break;
            case HomeHeaderBean.TYPE_JCB:
                openAnimeListActivity(Utils.getString(R.string.home_jcb_title), Sakura.JCB_API, false);
                break;
            case HomeHeaderBean.TYPE_DMFL_SILISILI_ZT:
                bundle.putString("title", Utils.getString(R.string.home_zt_title2));
                bundle.putString("url", Sakura.YHDM_ZT_API);
                startActivity(new Intent(getActivity(), AnimeTopicActivity.class).putExtras(bundle));
                break;
            case HomeHeaderBean.TYPE_DMFL_SILISILI_XFRM:
                siliParams[2] = String.format(siliArrStr, Api.SLILISILI_XFRM);
                openSiliTagList("新番日漫", String.format(tagUrl, "vodshow", "id", Api.SLILISILI_XFRM), siliParams);
                break;
            case HomeHeaderBean.TYPE_DMFL_SILISILI_XFGM:
                siliParams[2] = String.format(siliArrStr, Api.SILISILI_XFGM);
                openSiliTagList("新番国漫", String.format(tagUrl, "vodshow", "id", Api.SILISILI_XFGM), siliParams);
                break;
            case HomeHeaderBean.TYPE_DMFL_SILISILI_WJDM:
                siliParams[2] = String.format(siliArrStr, Api.SILISILI_WJDM);
                openSiliTagList("完结动漫", String.format(tagUrl, "vodshow", "id", Api.SILISILI_WJDM), siliParams);
                break;
            case HomeHeaderBean.TYPE_DMFL_SILISILI_JCB:
                siliParams[2] = String.format(siliArrStr, Api.SILISILI_JCB);
                openSiliTagList("剧场版", String.format(tagUrl, "vodshow", "id", Api.SILISILI_JCB), siliParams);
                break;
            case HomeHeaderBean.TYPE_DMFL_SILISILI_PHB:
                startActivity(new Intent(getActivity(), RankActivity.class));
                break;
        }
    }

    @Override
    public void onAnimeClick(HomeBean.HomeItemBean data) {
        Bundle bundle = new Bundle();
        bundle.putString("name", data.getTitle());
        String sakuraUrl = data.getUrl();
        bundle.putString("url", sakuraUrl);
        startActivity(new Intent(getActivity(), DescActivity.class).putExtras(bundle));
    }

    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
        ref.setVisibility(View.GONE);
        application.error = "";
        application.week = new JSONObject();
    }

    @OnClick(R.id.ref)
    public void refData() {
        ref.setVisibility(View.GONE);
        mSwipe.setRefreshing(true);
        multiItemEntities.clear();
        adapter.setNewData(multiItemEntities);
        loadData();
    }

    @Override
    public void showLoadErrorView(String msg) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            mSwipe.setRefreshing(false);
//            application.showErrorToastMsg(msg);
            CustomToast.showToast(getActivity(), msg, CustomToast.ERROR);
            /*errorTitle.setText(msg);
            adapter.setEmptyView(errorView);*/
            mSwipe.setEnabled(false);
            ref.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void showEmptyVIew() {

    }

    @Override
    public void showLog(String url) {

    }

    @Override
    public void showLoadSuccess(LinkedHashMap map) {

    }

    @Override
    public void showHomeLoadSuccess(List<HomeBean> beans) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
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
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("新番日漫", R.drawable.baseline_movie_white_48dp, HomeHeaderBean.TYPE_DMFL_SILISILI_XFRM));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("新番国漫", R.drawable.baseline_movie_white_48dp, HomeHeaderBean.TYPE_DMFL_SILISILI_XFGM));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("完结动漫", R.drawable.baseline_movie_white_48dp, HomeHeaderBean.TYPE_DMFL_SILISILI_WJDM));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("动漫专题", R.drawable.outline_video_library_white_48dp, HomeHeaderBean.TYPE_DMFL_SILISILI_ZT));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("剧场版", R.drawable.ic_ondemand_video_white_48dp, HomeHeaderBean.TYPE_DMFL_SILISILI_JCB));
                headerDataBeans.add(new HomeHeaderBean.HeaderDataBean("排行榜", R.drawable.ic_baseline_format_list_numbered_24, HomeHeaderBean.TYPE_DMFL_SILISILI_PHB));
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
        application.animeUpdateInfoBeans = beans;
    }
}
