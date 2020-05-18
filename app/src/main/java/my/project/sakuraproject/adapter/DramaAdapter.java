package my.project.sakuraproject.adapter;

import android.content.Context;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;

public class DramaAdapter extends BaseQuickAdapter<AnimeDescDetailsBean, BaseViewHolder> {
    private Context context;

    public DramaAdapter(Context context, @Nullable List<AnimeDescDetailsBean> data) {
        super(R.layout.item_btn, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, AnimeDescDetailsBean item) {
        Button btn = helper.getView(R.id.tag_group);
        helper.setText(R.id.tag_group, item.getTitle().replaceAll("第", "").replaceAll("集", ""));
        if (item.isSelected()) {
            helper.getView(R.id.tag_group).setBackgroundResource(R.drawable.button_selected);
            btn.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
        }
        else {
            helper.getView(R.id.tag_group).setBackgroundResource(R.drawable.button_default);
            btn.setTextColor(context.getResources().getColor(R.color.text_color_primary));
        }
    }
}
