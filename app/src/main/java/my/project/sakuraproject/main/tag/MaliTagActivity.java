package my.project.sakuraproject.main.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.AnimeListAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.MaliTagBean;
import my.project.sakuraproject.bean.TagBean;
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
    private AnimeListPresenter animeListPresenter;
    private int nowPage = 1;
    private int pageCount = 1;
    private boolean isErr = true;
    // ===========================================
    private String[] params = new String[] {"1", "", "", "", "", ""}; // 默认为全部
    private String subTitles = "%s%s%s%s";
    private String[] subTitlesArray = new String[] {"全部", "全部", "全部", "全部"};
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
    // TAG
    private List<MaliTagBean> maliTagBeans = new ArrayList<>();
    @BindView(R.id.chip_group)
    ChipGroup tagGroup;
    private BottomSheetDialog tagDialog;
    private ChipGroup itemChipsView;
    @BindView(R.id.ref_btn)
    MaterialButton refBtn;

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
        initSwipe();
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
            subTitlesArray[0] = title;
        }
    }

    private void initToolbar() {
        toolbar.setTitle( Utils.getString(R.string.tag_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
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

    @OnClick(R.id.ref_btn)
    public void refBtnClick() {
        refBtn.setVisibility(View.GONE);
        mPresenter = createPresenter();
        mPresenter.loadData(true);
    }


    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
    }

    @Override
    public void showSuccessView(List<TagBean> list) {

    }

    @Override
    public void showMaliSuccessView(List<MaliTagBean> list) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                mSwipe.setRefreshing(false);
                maliTagBeans = list;
                for (int i=0,size=maliTagBeans.size(); i<size; i++) {
                    Chip chip = new Chip(this);
                    switch (maliTagBeans.get(i).getTitle()) {
                        case FL:
                            chip.setText(FL + "：" + subTitlesArray[0]);
                            break;
                        case LX:
                            chip.setText(LX + "：" + subTitlesArray[1]);
                            break;
                        case NF:
                            chip.setText(NF + "：" + subTitlesArray[2]);
                            break;
                        case ZM:
                            chip.setText(ZM + "：" + subTitlesArray[3]);
                            break;
                    }
                    chip.setBackgroundColor(getResources().getColor(R.color.window_bg));
                    chip.setTextColor(getResources().getColor(R.color.text_color_primary));
                    chip.setChipStrokeColorResource(R.color.head);
                    int index = i;
                    chip.setOnClickListener(view -> {
                        openSelectDialog(maliTagBeans.get(index).getTitle(), index);
                    });
                    tagGroup.addView(chip);
                }
                if (!homeParam.isEmpty()) {
                    animeListPresenter = new AnimeListPresenter(params, this);
                    animeListPresenter.loadMaliData(true);
                }
            }
        });
    }

    private void openSelectDialog(String type, int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tag, null);
        TextView textView = view.findViewById(R.id.title);
        textView.setText(maliTagBeans.get(position).getTitle());
        ExtendedFloatingActionButton button = view.findViewById(R.id.confirm);
        itemChipsView = view.findViewById(R.id.chip_group);
        for (MaliTagBean.MaliTagList maliTagList : maliTagBeans.get(position).getMaliTagLists()) {
            Chip singleChip = (Chip) LayoutInflater.from(this).inflate(R.layout.dialog_chip, null);
            singleChip.setText(maliTagList.getItemTitle());
            switch (type) {
                case FL:
                    singleChip.setChecked(maliTagList.getItemTitle().equals(subTitlesArray[0]));
                    break;
                case LX:
                    singleChip.setChecked(maliTagList.getItemTitle().equals(subTitlesArray[1]));
                    break;
                case NF:
                    singleChip.setChecked(maliTagList.getItemTitle().equals(subTitlesArray[2]));
                    break;
                case ZM:
                    singleChip.setChecked(maliTagList.getItemTitle().equals(subTitlesArray[3]));
                    break;
            }
            singleChip.setBackgroundColor(getResources().getColor(R.color.window_bg));
            singleChip.setTextColor(getResources().getColor(R.color.text_color_primary));
            singleChip.setChipStrokeColorResource(R.color.head);
            singleChip.setOnCheckedChangeListener((compoundButton, b) -> {
                String title = compoundButton.getText().toString();
                if (b) {
                    for (int i = 0; i < itemChipsView.getChildCount(); i++) {
                        Chip chip1 = (Chip) itemChipsView.getChildAt(i);
                        chip1.setChecked(chip1.getText().toString().equals(title));
                    }
                    for (MaliTagBean maliTagBean : maliTagBeans) {
                        for (MaliTagBean.MaliTagList tagList : maliTagBean.getMaliTagLists()) {
                            if (tagList.getItemTitle().equals(title)) {
                                tagList.setSelected(true);
                                switch (type) {
                                    case FL:
                                        params[0] = getFlParam(tagList.getItemTitle());
                                        subTitlesArray[0] = tagList.getItemTitle();
                                        break;
                                    case LX:
                                        params[2] = tagList.getItemTitle().equals("全部") ? "" :  tagList.getItemTitle();
                                        subTitlesArray[1] = tagList.getItemTitle();
                                        break;
                                    case NF:
                                        params[5] = tagList.getItemTitle().equals("全部") ? "" :  tagList.getItemTitle();
                                        subTitlesArray[2] = tagList.getItemTitle();
                                        break;
                                    case ZM:
                                        params[3] = tagList.getItemTitle().equals("全部") ? "" :  tagList.getItemTitle();
                                        subTitlesArray[3] = tagList.getItemTitle();
                                        break;
                                }
                            } else
                                tagList.setSelected(false);
                        }
                    }
                } else {
                    switch (type) {
                        case FL:
                            params[0] = "1";
                            subTitlesArray[0] = "全部";
                            break;
                        case LX:
                            params[2] = "";
                            subTitlesArray[1] = "全部";
                            break;
                        case NF:
                            params[5] = "";
                            subTitlesArray[2] = "全部";
                            break;
                        case ZM:
                            params[3] = "";
                            subTitlesArray[3] = "全部";
                            break;
                    }
                }
            });
            itemChipsView.addView(singleChip);
        }
        tagDialog = new BottomSheetDialog(this);
        tagDialog.setContentView(view);
        button.setOnClickListener(view1 -> {
            if (!checkState()) return;
            tagDialog.dismiss();
            Chip tag;
            switch (type) {
                case FL:
                    tag = (Chip) tagGroup.getChildAt(0);
                    tag.setText(FL + "：" + subTitlesArray[0]);
                    break;
                case LX:
                    tag = (Chip) tagGroup.getChildAt(1);
                    tag.setText(LX + "：" + subTitlesArray[1]);
                    break;
                case NF:
                    tag = (Chip) tagGroup.getChildAt(2);
                    tag.setText(NF + "：" + subTitlesArray[2]);
                    break;
                case ZM:
                    tag = (Chip) tagGroup.getChildAt(3);
                    tag.setText(ZM + "：" + subTitlesArray[3]);
                    break;
            }
            nowPage = 1;
            params[4] = nowPage + "";
            getTagData();
        });
        tagDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        tagDialog.show();
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                refBtn.setVisibility(View.VISIBLE);
                setRecyclerViewEmpty();
                mSwipe.setRefreshing(false);
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
