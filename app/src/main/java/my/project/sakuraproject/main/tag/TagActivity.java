package my.project.sakuraproject.main.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

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
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.main.animeList.AnimeListContract;
import my.project.sakuraproject.main.animeList.AnimeListPresenter;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.util.Utils;

public class TagActivity extends BaseActivity<TagContract.View, TagPresenter> implements TagContract.View, AnimeListContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView animeListRecyclerView;
    private AnimeListAdapter animeListAdapter;
    private List<AnimeListBean> animeLists = new ArrayList<>();
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private AnimeListPresenter animeListPresenter;
    private String animeUrl = "";
    private String tagUrl = "";
    private String title = "";
    private String[] siliParams;
    private List<String> siliTagParams = new ArrayList<>();
    private List<String> siliTagSubTitle = new ArrayList<>();
    private String toolbarSubTitle;
    private int nowPage = 1;
    private int pageCount = 1;
    private boolean isErr = true;
    // TAG
    private List<TagBean> tagBeans = new ArrayList<>();
    @BindView(R.id.chip_group)
    ChipGroup tagGroup;
    private BottomSheetDialog tagDialog;
    private ChipGroup itemChipsView;
    @BindView(R.id.ref_btn)
    MaterialButton refBtn;

    @Override
    protected TagPresenter createPresenter() {
        return new TagPresenter(Utils.isImomoe() ? tagUrl : Sakura.TAG_API, siliParams, this);
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
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && !bundle.isEmpty()) {
            animeUrl = bundle.getString("url") == null ? "" : bundle.getString("url");
            tagUrl = bundle.getString("tagUrl") == null ? "" : bundle.getString("tagUrl");
            title = bundle.getString("title");
            siliParams = bundle.getStringArray("siliParams");
        }

    }

    public void initToolbar() {
        toolbar.setTitle(title.isEmpty() ? Utils.getString(R.string.tag_title) : title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initSwipe() {
        mSwipe.setEnabled(false);
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            animeLists.clear();
            animeListAdapter.setNewData(animeLists);
            nowPage = 1;
            animeListPresenter = new AnimeListPresenter(Utils.isImomoe() ? tagUrl : animeUrl, siliTagParams, nowPage, this);
            animeListPresenter.loadData(true, false, Utils.isImomoe(), false);
        });
    }

    public void initAdapter() {
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
            startActivity(new Intent(TagActivity.this, DescActivity.class).putExtras(bundle));
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
                    animeListPresenter = new AnimeListPresenter(Utils.isImomoe() ? tagUrl : animeUrl, siliTagParams, nowPage, this);
                    animeListPresenter.loadData(false, false, Utils.isImomoe(), false);
                } else {
                    //获取更多数据失败
                    isErr = true;
                    animeListAdapter.loadMoreFail();
                }
            }
        }, 500), animeListRecyclerView);
        animeListRecyclerView.setAdapter(animeListAdapter);
    }

    public void setLoadState(boolean loadState) {
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
    public void showTagSuccessView(boolean isSilisili, List<TagBean> list) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
//                ref.setVisibility(View.GONE);
                mSwipe.setRefreshing(false);
                tagBeans = list;
                for (int i=0,size=tagBeans.size(); i<size; i++) {
                    Chip chip = new Chip(this);
                    chip.setText(tagBeans.get(i).getTitle());
                    chip.setBackgroundColor(getResources().getColor(R.color.window_bg));
                    chip.setTextColor(getResources().getColor(R.color.text_color_primary));
                    chip.setChipStrokeColorResource(R.color.head);
                    chip.setRippleColor(getResources().getColorStateList(R.color.ripple_color));
                    int index = i;
                    chip.setOnClickListener(view -> {
                        if (isSilisili)
                            openSiliSelectDialog(index);
                        else
                            openSelectDialog(index);
                    });
                    tagGroup.addView(chip);
                }
                if (!animeUrl.isEmpty()) {
                    animeListPresenter = new AnimeListPresenter(Utils.isImomoe() ? tagUrl : animeUrl, siliTagParams, nowPage, this);
                    animeListPresenter.loadData(true, false, Utils.isImomoe(), false);
                }
            }
        });
    }

    @Override
    public void showDefaultSiliAnimeList(List<AnimeListBean> animeListBeans, int pageCount) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                toolbar.setSubtitle("[全部]");
                this.pageCount = pageCount;
                setRecyclerViewView();
                animeLists = animeListBeans;
                animeListAdapter.setNewData(animeLists);
            }
        });
    }

    private void openSelectDialog(int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tag, null);
        TextView textView = view.findViewById(R.id.title);
        textView.setText(tagBeans.get(position).getTitle());
        ExtendedFloatingActionButton button = view.findViewById(R.id.confirm);
        itemChipsView = view.findViewById(R.id.chip_group);
        for (TagBean.TagSelectBean tagSelectBean : tagBeans.get(position).getTagSelectBeans()) {
            Chip singleChip = (Chip) LayoutInflater.from(this).inflate(R.layout.dialog_chip, null);
            singleChip.setText(tagSelectBean.getTitle());
            if (animeUrl.equals(tagSelectBean.getUrl()))
                singleChip.setChecked(true);
            singleChip.setOnCheckedChangeListener((compoundButton, b) -> {
                String title = compoundButton.getText().toString();
                if (b) {
                    for (int i = 0; i < itemChipsView.getChildCount(); i++) {
                        Chip chip1 = (Chip) itemChipsView.getChildAt(i);
                        chip1.setChecked(chip1.getText().toString().equals(title));
                    }
                    for (TagBean tagBean : tagBeans) {
                        for (TagBean.TagSelectBean selectBean : tagBean.getTagSelectBeans()) {
                            if (selectBean.getTitle().equals(title)) {
                                selectBean.setSelected(true);
                                animeUrl = selectBean.getUrl();
                                toolbarSubTitle = selectBean.getTitle();
                            } else
                                selectBean.setSelected(false);
                        }
                    }
                } else {
                    animeUrl = "";
                    for (int i=0,size=tagBeans.get(position).getTagSelectBeans().size(); i<size; i++) {
                        tagBeans.get(position).getTagSelectBeans().get(i).setSelected(false);
                    }
                }
            });
            itemChipsView.addView(singleChip);
        }
        tagDialog = new BottomSheetDialog(this);
        tagDialog.setContentView(view);
        button.setOnClickListener(view1 -> {
            tagDialog.dismiss();
            if (animeUrl.isEmpty())
                return;
            toolbar.setSubtitle(toolbarSubTitle);
            animeLists.clear();
            animeListAdapter.setNewData(null);
            animeListAdapter.setEmptyView(getLayoutInflater().inflate(R.layout.base_emnty_view, null));
            nowPage = 1;
            animeListPresenter = new AnimeListPresenter(Utils.isImomoe() ? tagUrl : animeUrl, siliTagParams, nowPage, this);
            animeListPresenter.loadData(true, false, Utils.isImomoe(), false);
        });
        tagDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        tagDialog.show();
    }

    private void openSiliSelectDialog(int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tag, null);
        TextView textView = view.findViewById(R.id.title);
        textView.setText(tagBeans.get(position).getTitle());
        ExtendedFloatingActionButton button = view.findViewById(R.id.confirm);
        itemChipsView = view.findViewById(R.id.chip_group);
        for (TagBean.TagSelectBean tagSelectBean : tagBeans.get(position).getTagSelectBeans()) {
            Chip singleChip = (Chip) LayoutInflater.from(this).inflate(R.layout.dialog_chip, null);
            singleChip.setText(tagSelectBean.getTitle());
            if (siliTagParams.contains(tagSelectBean.getUrl()))
                singleChip.setChecked(true);
            singleChip.setOnCheckedChangeListener((compoundButton, b) -> {
                String title = compoundButton.getText().toString();
                if (b) {
                    for (int i = 0; i < itemChipsView.getChildCount(); i++) {
                        Chip chip1 = (Chip) itemChipsView.getChildAt(i);
                        chip1.setChecked(chip1.getText().toString().equals(title));
                    }
                    for (TagBean.TagSelectBean selectBean : tagBeans.get(position).getTagSelectBeans()) {
                        if (selectBean.getTitle().equals(title)) {
                            selectBean.setSelected(true);
                            toolbarSubTitle = selectBean.getTitle();
                            siliTagParams.add(selectBean.getUrl());
                            siliTagSubTitle.add(selectBean.getTitle());
                        } else {
                            if (siliTagParams.contains(selectBean.getUrl())) {
                                siliTagParams.remove(selectBean.getUrl());
                                siliTagSubTitle.remove(selectBean.getTitle());
                            }
                            selectBean.setSelected(false);
                        }
                    }
                } else {
                    for (int i=0,size=tagBeans.get(position).getTagSelectBeans().size(); i<size; i++) {
                        tagBeans.get(position).getTagSelectBeans().get(i).setSelected(false);
                        TagBean.TagSelectBean checkedBean = tagBeans.get(position).getTagSelectBeans().get(i);
                        if (siliTagParams.contains(checkedBean.getUrl())) {
                            siliTagParams.remove(checkedBean.getUrl());
                            siliTagSubTitle.remove(checkedBean.getTagTitle());
                        }
                    }
                }
            });
            itemChipsView.addView(singleChip);
        }
        tagDialog = new BottomSheetDialog(this);
        tagDialog.setContentView(view);
        button.setOnClickListener(view1 -> {
            tagDialog.dismiss();
            toolbar.setSubtitle(siliTagSubTitle.size() > 0 ? siliTagSubTitle.toString() : "[全部]");
            animeLists.clear();
            animeListAdapter.setNewData(null);
            animeListAdapter.setEmptyView(getLayoutInflater().inflate(R.layout.base_emnty_view, null));
            nowPage = 1;
            animeListPresenter = new AnimeListPresenter(Utils.isImomoe() ? tagUrl : animeUrl, siliTagParams, nowPage, this);
            animeListPresenter.loadData(true, false, Utils.isImomoe(), false);
        });
        tagDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        tagDialog.show();
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
//                ref.setVisibility(View.VISIBLE);
                setRecyclerViewEmpty();
                mSwipe.setRefreshing(false);
                refBtn.setVisibility(View.VISIBLE);
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
        if (!Utils.isPad()) {
            animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 3 : 5));
        }
        else {
            if (isInMagicWindow) {
                animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            } else {
                animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 5 : 8));
            }
        }
        animeListRecyclerView.getLayoutManager().scrollToPosition(position);
    }
}
