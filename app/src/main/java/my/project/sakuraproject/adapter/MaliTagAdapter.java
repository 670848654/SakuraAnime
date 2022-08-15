package my.project.sakuraproject.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.MaliTagBean;

/**
 * MALIMALI分类适配器
 */
public class MaliTagAdapter extends BaseQuickAdapter<MaliTagBean.MaliTagList, BaseViewHolder> {
    private Context context;

    public MaliTagAdapter(Context context, @Nullable List<MaliTagBean.MaliTagList> data) {
        super(R.layout.item_mali_tag_item, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, MaliTagBean.MaliTagList item) {
        helper.setText(R.id.tag_group, item.getItemTitle());
        helper.setTextColor(R.id.tag_group, item.isSelected() ? context.getResources().getColor(R.color.colorAccent) : context.getResources().getColor(R.color.text_color_primary));
    }
}
