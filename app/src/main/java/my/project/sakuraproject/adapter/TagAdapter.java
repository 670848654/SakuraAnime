package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.bean.TagHeaderBean;

public class TagAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    public static final int TYPE_LEVEL_0 = 0;
    public static final int TYPE_LEVEL_1 = 1;
    private Context context;

    public TagAdapter(Context context, List data) {
        super(data);
        this.context = context;
        addItemType(TYPE_LEVEL_0, R.layout.item_tag_head);
        addItemType(TYPE_LEVEL_1, R.layout.item_tag_item);
    }

    @Override
    protected void convert(final BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case TYPE_LEVEL_0:
                final TagHeaderBean homeHeaderBean = (TagHeaderBean) item;
                helper.setText(R.id.header, homeHeaderBean.getTitle());
//                helper.setText(R.id.header, homeHeaderBean.getTitle()).setImageResource(R.id.arrow, homeHeaderBean.isExpanded() ? R.drawable.ic_keyboard_arrow_down_white_48dp : R.drawable.baseline_keyboard_arrow_right_white_48dp);
                helper.itemView.setOnClickListener(v -> {
                   /* int pos = helper.getAdapterPosition();
                    if (homeHeaderBean.isExpanded()) {
                        collapse(pos);
                    } else {
                        expand(pos);
                    }*/
                });
                break;
            case TYPE_LEVEL_1:
                TagBean tagBean = (TagBean) item;
                helper.setText(R.id.tag_group, tagBean.getItemTitle());
                helper.setTextColor(R.id.tag_group, tagBean.isSelected() ? context.getResources().getColor(R.color.colorAccent) : context.getResources().getColor(R.color.text_color_primary));
                break;
        }
    }
}
