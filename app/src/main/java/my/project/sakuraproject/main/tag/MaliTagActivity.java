package my.project.sakuraproject.main.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.AnimeListAdapter;
import my.project.sakuraproject.adapter.MaliTagAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.MaliTagBean;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.main.animeList.AnimeListContract;
import my.project.sakuraproject.main.animeList.AnimeListPresenter;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.util.Utils;

/**
 * MALIMALI分类界面
 */
public class MaliTagActivity extends BaseActivity<TagContract.View, TagPresenter> implements TagContract.View, AnimeListContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView animeListRecyclerView;
    private AnimeListAdapter animeListAdapter;
    private List<AnimeListBean> animeLists = new ArrayList<>();
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private BottomSheetDialog mBottomSheetDialog;
    private MaterialButton ref;
    @BindView(R.id.tag_btn)
    FloatingActionButton tag_btn;
    private AnimeListPresenter animeListPresenter;
    private int nowPage = 1;
    private int pageCount = 1;
    private boolean isErr = true;
    // ===========================================
    private String[] params = new String[] {"1", "", "", "", "", ""}; // 默认为全部
    private String subTitles = "%s%s%s%s";
    private String[] subTitlesArray = new String[] {"{全部}", "", "", ""};
    private final static String FL_ALL = "全部";
    public final static String FL_CHINA = "国产动漫";
    public final static String FL_JAPAN = "日韩动漫";
    public final static String FL_EUROPE = "欧美动漫";
    /*private final static String FL = "全部类型";
    private final static String LX = "全部剧情";
    private final static String NF = "全部时间";
    private final static String ZM = "全部字母";*/
    private final static String FL = "分类";
    private final static String LX = "类型";
    private final static String NF = "年份";
    private final static String ZM = "字母";
    private String homeParam = "";
    private String title = "";
    private LinearLayout listView;
    // 分类
    private RecyclerView flRecyclerView;
    private MaliTagAdapter flAdapter;
    // 类型
    private RecyclerView lxRecyclerView;
    private MaliTagAdapter lxAdapter;
    // 年份
    private RecyclerView nfRecyclerView;
    private MaliTagAdapter nfAdapter;
    // 字母
    private RecyclerView zmRecyclerView;
    private MaliTagAdapter zmAdapter;


    @Override
    protected TagPresenter createPresenter() {
        return new TagPresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_tag;
    }

    @Override
    protected void init() {
//        Slidr.attach(this, Utils.defaultInit());
        getBundle();
        initToolbar();
        initFab();
        initSwipe();
        initTagAdapter();
        initAnimeAdapter();
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && !bundle.isEmpty()) {
            homeParam = bundle.getString("homeParam");
            params[0] = homeParam;
            title = bundle.getString("title");
            subTitlesArray[0] = "{" + title + "}";
        }
    }

    private void initToolbar() {
//        toolbar.setTitle(title.isEmpty() ? Utils.getString(R.string.tag_title) : title);
        toolbar.setTitle( Utils.getString(R.string.tag_title));
        setToolbarSubTitle();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void setToolbarSubTitle() {
        toolbar.setSubtitle(String.format(subTitles, subTitlesArray[0], subTitlesArray[1], subTitlesArray[2], subTitlesArray[3]));
    }

    private void initFab() {
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tag_btn.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 15);
            tag_btn.setLayoutParams(params);
        }
    }

    private void initSwipe() {
        mSwipe.setEnabled(false);
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            animeLists.clear();
            animeListAdapter.setNewData(animeLists);
            nowPage = 1;
            params[4] = nowPage + "";
            animeListPresenter = new AnimeListPresenter(params, this);
            animeListPresenter.loadMaliData(true);
        });
    }

    private boolean checkState() {
        if (!Utils.isFastClick()) return false;
        if (mSwipe.isRefreshing()) {
//                Sakura.getInstance().showToastMsg(Utils.getString(R.string.loading_info));
            CustomToast.showToast(this, Utils.getString(R.string.loading_info), CustomToast.WARNING);
            return false;
        }
        return true;
    }

    private String getFlParam(String fl) {
        switch (fl) {
            case FL_ALL:
                return Api.MALIMALI_TAG_DEFAULT;
            case FL_JAPAN:
                return Api.MALIMALI_JAPAN;
            case FL_CHINA:
                return Api.MALIMALI_CHINA;
            case FL_EUROPE:
                return Api.MALIMALI_EUROPE;
        }
        return "";
    }

    private void getTagData() {
        animeLists.clear();
        animeListAdapter.setNewData(null);
        animeListAdapter.setEmptyView(getLayoutInflater().inflate(R.layout.base_emnty_view, null));
        animeListPresenter = new AnimeListPresenter(params, this);
        animeListPresenter.loadMaliData(true);
    }

    private void initTagAdapter() {
        View tagView = LayoutInflater.from(this).inflate(R.layout.dialog_mali_tag, null);
        listView = tagView.findViewById(R.id.list_view);
        // 分类数据
        flAdapter = new MaliTagAdapter(this, new ArrayList<>());
        flRecyclerView = tagView.findViewById(R.id.fl_list);
        setRecyclerViewHorizontal(flRecyclerView);
        flRecyclerView.setAdapter(flAdapter);
        flAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (checkState()) {
                // 设置选中颜色
                TextView textView = (TextView) adapter.getViewByPosition(flRecyclerView, position, R.id.tag_group);
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                MaliTagBean.MaliTagList bean = (MaliTagBean.MaliTagList) adapter.getItem(position);
                List<MaliTagBean.MaliTagList> maliTagLists = adapter.getData();
                for (MaliTagBean.MaliTagList tagBean : maliTagLists) {
                    tagBean.setSelected(false);
                }
                bean.setSelected(true);
                subTitlesArray[0] = "{" + bean.getItemTitle() + "}";
                setToolbarSubTitle();
                adapter.setNewData(maliTagLists);
                mSwipe.setEnabled(true);
                // 刷新动漫列表
                params[0] = getFlParam(bean.getItemTitle());
                nowPage = 1;
                params[4] = nowPage + "";
                getTagData();
            }
        });
        // 类型数据
        lxAdapter = new MaliTagAdapter(this, new ArrayList<>());
        lxRecyclerView = tagView.findViewById(R.id.lx_list);
        setRecyclerViewHorizontal(lxRecyclerView);
        lxRecyclerView.setAdapter(lxAdapter);
        lxAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (checkState()) {
                // 设置选中颜色
                TextView textView = (TextView) adapter.getViewByPosition(lxRecyclerView, position, R.id.tag_group);
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                MaliTagBean.MaliTagList bean = (MaliTagBean.MaliTagList) adapter.getItem(position);
                List<MaliTagBean.MaliTagList> maliTagLists = adapter.getData();
                for (MaliTagBean.MaliTagList tagBean : maliTagLists) {
                    tagBean.setSelected(false);
                }
                bean.setSelected(true);
                subTitlesArray[1] = "{" + bean.getItemTitle() + "}";
                setToolbarSubTitle();
                adapter.setNewData(maliTagLists);
                mSwipe.setEnabled(true);
                // 刷新动漫列表
                params[2] = bean.getItemTitle().equals("全部") ? "" :  bean.getItemTitle();
//                params[2] = bean.getItemTitle().contains("全部") ? "" :  bean.getItemTitle();
                nowPage = 1;
                params[4] = nowPage + "";
                getTagData();
            }
        });
        // 年份数据
        nfAdapter = new MaliTagAdapter(this, new ArrayList<>());
        nfRecyclerView = tagView.findViewById(R.id.nf_list);
        setRecyclerViewHorizontal(nfRecyclerView);
        nfRecyclerView.setAdapter(nfAdapter);
        nfAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (checkState()) {
                // 设置选中颜色
                TextView textView = (TextView) adapter.getViewByPosition(nfRecyclerView, position, R.id.tag_group);
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                MaliTagBean.MaliTagList bean = (MaliTagBean.MaliTagList) adapter.getItem(position);
                List<MaliTagBean.MaliTagList> maliTagLists = adapter.getData();
                for (MaliTagBean.MaliTagList tagBean : maliTagLists) {
                    tagBean.setSelected(false);
                }
                bean.setSelected(true);
                subTitlesArray[2] = "{" + bean.getItemTitle() + "}";
                setToolbarSubTitle();
                adapter.setNewData(maliTagLists);
                mSwipe.setEnabled(true);
                // 刷新动漫列表
                params[5] = bean.getItemTitle().equals("全部") ? "" :  bean.getItemTitle();
//                params[5] = bean.getItemTitle().contains("全部") ? "" :  bean.getItemTitle();
                nowPage = 1;
                params[4] = nowPage + "";
                getTagData();
            }
        });
        // 字母数据
        zmAdapter = new MaliTagAdapter(this, new ArrayList<>());
        zmRecyclerView = tagView.findViewById(R.id.zm_list);
        setRecyclerViewHorizontal(zmRecyclerView);
        zmRecyclerView.setAdapter(zmAdapter);
        zmAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (checkState()) {
                // 设置选中颜色
                TextView textView = (TextView) adapter.getViewByPosition(zmRecyclerView, position, R.id.tag_group);
                textView.setTextColor(getResources().getColor(R.color.colorAccent));
                MaliTagBean.MaliTagList bean = (MaliTagBean.MaliTagList) adapter.getItem(position);
                List<MaliTagBean.MaliTagList> maliTagLists = adapter.getData();
                for (MaliTagBean.MaliTagList tagBean : maliTagLists) {
                    tagBean.setSelected(false);
                }
                bean.setSelected(true);
                subTitlesArray[3] = "{" + bean.getItemTitle() + "}";
                setToolbarSubTitle();
                adapter.setNewData(maliTagLists);
                mSwipe.setEnabled(true);
                // 刷新动漫列表
                params[3] = bean.getItemTitle().equals("全部") ? "" :  bean.getItemTitle();
//                params[3] = bean.getItemTitle().contains("全部") ? "" :  bean.getItemTitle();
                nowPage = 1;
                params[4] = nowPage + "";
                getTagData();
            }
        });

        ref = tagView.findViewById(R.id.ref);
        ref.setOnClickListener((view)-> {
            mBottomSheetDialog.dismiss();
            mPresenter = createPresenter();
            mPresenter.loadData(true);
        });
        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(tagView);
    }

    private void setRecyclerViewHorizontal(RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initAnimeAdapter() {
        // 动漫列表数据
        animeListAdapter = new AnimeListAdapter(this, animeLists, false);
        animeListAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        animeListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            final AnimeListBean bean = (AnimeListBean) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("name", bean.getTitle());
            String diliUrl = bean.getUrl();
            bundle.putString("url", diliUrl);
            startActivity(new Intent(MaliTagActivity.this, DescActivity.class).putExtras(bundle));
        });
        if (Utils.checkHasNavigationBar(this)) animeListRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        animeListAdapter.setLoadMoreView(new CustomLoadMoreView());
        animeListAdapter.setOnLoadMoreListener(() -> animeListRecyclerView.postDelayed(() -> {
            if (nowPage >= pageCount) {
                //数据全部加载完毕
                animeListAdapter.loadMoreEnd();
//                application.showSuccessToastMsg(Utils.getString(R.string.no_more));
                CustomToast.showToast(this, Utils.getString(R.string.no_more), CustomToast.SUCCESS);
            } else {
                if (isErr) {
                    //成功获取更多数据
                    nowPage++;
                    params[4] = nowPage + "";
                    animeListPresenter = new AnimeListPresenter(params,this);
                    animeListPresenter.loadMaliData(false);
                } else {
                    //获取更多数据失败
                    isErr = true;
                    animeListAdapter.loadMoreFail();
                }
            }
        }, 500), animeListRecyclerView);
        animeListRecyclerView.setAdapter(animeListAdapter);
    }

    private void setLoadState(boolean loadState) {
        isErr = loadState;
        animeListAdapter.loadMoreComplete();
    }

    @OnClick(R.id.tag_btn)
    public void tagBtnClick() {
        mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        mBottomSheetDialog.show();
    }


    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
    }

    @Override
    public void showSuccessView(List<MultiItemEntity> list) {

    }

    @Override
    public void showMaliSuccessView(List<MaliTagBean> list) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                ref.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                mSwipe.setRefreshing(false);
                for (MaliTagBean beans : list) {
                    switch (beans.getTitle()) {
                        case FL:
                            for (MaliTagBean.MaliTagList bean : beans.getMaliTagLists()) {
                                if (subTitlesArray[0].contains(bean.getItemTitle())) {
                                    bean.setSelected(true);
                                }
                            }
                            flAdapter.setNewData(beans.getMaliTagLists());
                            break;
                        case LX:
                            beans.getMaliTagLists().get(0).setSelected(true);
                            lxAdapter.setNewData(beans.getMaliTagLists());
                            break;
                        case NF:
                            beans.getMaliTagLists().get(0).setSelected(true);
                            nfAdapter.setNewData(beans.getMaliTagLists());
                            break;
                        case ZM:
                            beans.getMaliTagLists().get(0).setSelected(true);
                            zmAdapter.setNewData(beans.getMaliTagLists());
                            break;
                    }
                }
                if (!homeParam.isEmpty()) {
                    animeListPresenter = new AnimeListPresenter(params, this);
                    animeListPresenter.loadMaliData(true);
                } else {
                    mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                    mBottomSheetDialog.show();
                }
                tag_btn.show();
            }
        });
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                ref.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                setRecyclerViewEmpty();
                mSwipe.setRefreshing(false);
                errorTitle.setText(msg);
                mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                mBottomSheetDialog.show();
                tag_btn.show();
            }
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
    public void showSuccessView(boolean isMain, List<AnimeListBean> animeList) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (isMain) {
                    setRecyclerViewView();
                    mSwipe.setRefreshing(false);
                    animeLists = animeList;
                    animeListAdapter.setNewData(animeLists);
                } else {
                    animeListAdapter.addData(animeList);
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
                    setRecyclerViewEmpty();
                    mSwipe.setRefreshing(false);
                    errorTitle.setText(msg);
                    animeListAdapter.setEmptyView(errorView);
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
    public void onResume() {
        super.onResume();
        setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewEmpty() {
        animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void setRecyclerViewView() {
        position = animeListRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) animeListRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad())
            animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 3 : 5));
        else {
            if (isInMagicWindow)
                animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            else
                animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 5 : 8));
        }
        animeListRecyclerView.getLayoutManager().scrollToPosition(position);
    }
}
