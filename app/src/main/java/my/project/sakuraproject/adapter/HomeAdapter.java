package my.project.sakuraproject.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.transformer.DepthPageTransformer;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.HomeHeaderBean;

public class HomeAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    public static final int TYPE_LEVEL_0 = 0;
    public static final int TYPE_LEVEL_1 = 1;
    public static final int TYPE_LEVEL_2 = 2;
    private Context context;
    private RecyclerView recyclerView;
    private Banner banner;
    private HomeItemAdapter homeItemAdapter;
    private OnItemClick onItemClick;

    public HomeAdapter(Context context, List data, OnItemClick onItemClick) {
        super(data);
        this.context = context;
        this.onItemClick = onItemClick;
        addItemType(TYPE_LEVEL_0, R.layout.item_home_header);
        addItemType(TYPE_LEVEL_1, R.layout.item_banner);
        addItemType(TYPE_LEVEL_2, R.layout.item_home);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case TYPE_LEVEL_0:
                HomeHeaderBean homeHeaderBean = (HomeHeaderBean) item;
                ChipGroup chipGroup = helper.getView(R.id.chip_group);
                chipGroup.removeAllViews();
                /*recyclerView = helper.getView(R.id.header_list);
                List<HomeHeaderBean.HeaderDataBean> headerDataBeans = homeHeaderBean.getData();
                recyclerView.setLayoutManager(new GridLayoutManager(context, headerDataBeans.size()));
                homeHeaderAdapter = new HomeHeaderAdapter(context, headerDataBeans);
                homeHeaderAdapter.setOnItemClickListener((adapter, view, position) -> {
                    onItemClick.onHeaderClick(headerDataBeans.get(position));
                });
                recyclerView.setAdapter(homeHeaderAdapter);*/
                List<HomeHeaderBean.HeaderDataBean> headerDataBeans = homeHeaderBean.getData();
                for (HomeHeaderBean.HeaderDataBean headerDataBean : headerDataBeans) {
                    Chip chip = new Chip(context);
                    chip.setText(headerDataBean.getTitle());
                    chip.setBackgroundColor(context.getResources().getColor(R.color.window_bg));
                    chip.setChipIconResource(headerDataBean.getImg());
                    chip.setChipIconTint(ColorStateList.valueOf(context.getResources().getColor(R.color.text_color_primary)));
                    chip.setTextColor(context.getResources().getColor(R.color.text_color_primary));
                    chip.setChipStrokeColorResource(R.color.head);
                    chip.setRippleColor(context.getResources().getColorStateList(R.color.ripple_color));
                    chip.setOnClickListener(view -> {
                        onItemClick.onHeaderClick(headerDataBean);
                    });
                    chipGroup.addView(chip);
                }
                break;
            case TYPE_LEVEL_1:
                // banner
                HomeBean bannerBean = (HomeBean) item;
                List<HomeBean.HomeItemBean> bannerItem = bannerBean.getData();
                banner = helper.getView(R.id.banner);
                banner.setAdapter(new HomeBannerAdapter(context, bannerItem))
//                        .setBannerGalleryMZ(20)
                        .setIndicator(new CircleIndicator(context));
                banner.setOnBannerListener((data, position) -> {
                    onItemClick.onAnimeClick(bannerItem.get(position));
                });
                break;
            case TYPE_LEVEL_2:
                HomeBean homeBean = (HomeBean) item;
                List<HomeBean.HomeItemBean> homeItemBean = homeBean.getData();
                helper.setText(R.id.title, homeBean.getTitle());
                helper.setTextColor(R.id.title, context.getResources().getColor(R.color.text_color_primary));
                helper.setBackgroundColor(R.id.root, context.getResources().getColor(R.color.window_bg));
                helper.setBackgroundColor(R.id.more, context.getResources().getColor(R.color.window_bg));
                if (homeBean.getMoreUrl().isEmpty())
                    helper.getView(R.id.img).setVisibility(View.GONE);
                else
                    helper.getView(R.id.img).setVisibility(View.VISIBLE);
                ImageView img = helper.getView(R.id.img);
                img.setColorFilter(context.getResources().getColor(R.color.text_color_primary));
                helper.addOnClickListener(R.id.more);
                recyclerView = helper.getView(R.id.rv_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                homeItemAdapter = new HomeItemAdapter(context, homeItemBean);
                homeItemAdapter.setOnItemClickListener((adapter, view, position) -> {
                    onItemClick.onAnimeClick(homeItemBean.get(position));
                });
                recyclerView.setPadding(0,0,0, 10);
                recyclerView.setAdapter(homeItemAdapter);
                break;
        }
    }

    public interface OnItemClick {
        void onHeaderClick(HomeHeaderBean.HeaderDataBean bean);
        void onAnimeClick(HomeBean.HomeItemBean data);
    }
}