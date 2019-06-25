package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescBean;
import my.project.sakuraproject.bean.AnimeHeaderBean;
import my.project.sakuraproject.config.AnimeType;
import my.project.sakuraproject.util.Utils;

public class DescAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private Context context;

    public DescAdapter(Context context, List<MultiItemEntity> data) {
        super(data);
        this.context = context;
        addItemType(AnimeType.TYPE_LEVEL_0, R.layout.item_head);
        addItemType(AnimeType.TYPE_LEVEL_1, R.layout.item_btn);
        addItemType(AnimeType.TYPE_LEVEL_2, R.layout.item_favorite);
        addItemType(AnimeType.TYPE_LEVEL_3, R.layout.item_favorite);
    }

    @Override
    protected void convert(final BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case AnimeType.TYPE_LEVEL_0:
                final AnimeHeaderBean mainHeaderBean = (AnimeHeaderBean) item;
                helper.setText(R.id.header, mainHeaderBean.getTitle()).setImageResource(R.id.arrow, mainHeaderBean.isExpanded() ? R.drawable.ic_keyboard_arrow_down_white_48dp : R.drawable.baseline_keyboard_arrow_right_white_48dp);
                helper.itemView.setOnClickListener(v -> {
                    int pos = helper.getAdapterPosition();
                    if (mainHeaderBean.isExpanded()) {
                        collapse(pos);
                    } else {
                        expand(pos);
                    }
                });
                break;
            case AnimeType.TYPE_LEVEL_1:
                final AnimeDescBean animeDescBean = (AnimeDescBean) item;
                String title = animeDescBean.getTitle();
                if (animeDescBean.getType().equals("play"))
                    helper.setText(R.id.tag_group, title.replaceAll("第", "").replaceAll("集", ""));
                else
                    helper.setText(R.id.tag_group, title);
                if (animeDescBean.isSelect())
                    helper.getView(R.id.tag_group).setBackground(context.getResources().getDrawable(R.drawable.button_selected, null));
                else
                    helper.getView(R.id.tag_group).setBackground(context.getResources().getDrawable(R.drawable.button_default, null));
                break;
            case AnimeType.TYPE_LEVEL_2:
                final AnimeDescBean bean = (AnimeDescBean) item;
                helper.setText(R.id.title, bean.getTitle());
                Utils.setDefaultImage(context, bean.getImg(), helper.getView(R.id.img));
                Utils.setCardBg(context, bean.getImg(), helper.getView(R.id.card_view), helper.getView(R.id.title));
                break;
            case AnimeType.TYPE_LEVEL_3:
                final AnimeDescBean ova = (AnimeDescBean) item;
                helper.setText(R.id.title, ova.getTitle());
                Utils.setDefaultImage(context, ova.getImg(), helper.getView(R.id.img));
                Utils.setCardBg(context, ova.getImg(), helper.getView(R.id.card_view), helper.getView(R.id.title));
                break;
        }
    }
}
