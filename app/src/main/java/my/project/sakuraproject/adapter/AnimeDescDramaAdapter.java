package my.project.sakuraproject.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;

/**
 * 展开播放列表适配器
 * @deprecated 播放列表相同布局统一使用 {@link DramaAdapter}
 */
@Deprecated
public class AnimeDescDramaAdapter extends BaseQuickAdapter<AnimeDescDetailsBean, BaseViewHolder> {
    private Context context;

    public AnimeDescDramaAdapter(Context context, @Nullable List<AnimeDescDetailsBean> data) {
        super(R.layout.item_desc_drama, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, AnimeDescDetailsBean item) {
        MaterialButton materialButton = helper.getView(R.id.tag_group);
        helper.setText(R.id.tag_group, item.getTitle());
        materialButton.setTextColor(item.isSelected() ? context.getResources().getColor(R.color.tabSelectedTextColor) : context.getResources().getColor(R.color.text_color_primary));
    }
}