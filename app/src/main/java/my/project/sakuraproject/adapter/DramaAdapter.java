package my.project.sakuraproject.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.material.button.MaterialButton;

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
        MaterialButton materialButton = helper.getView(R.id.tag_group);
        helper.setText(R.id.tag_group, item.getTitle());
        if (item.isSelected())
            materialButton.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
        else
            materialButton.setTextColor(context.getResources().getColor(R.color.text_color_primary));
    }
}
