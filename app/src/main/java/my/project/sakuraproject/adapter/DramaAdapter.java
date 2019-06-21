package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;
import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescBean;

public class DramaAdapter extends BaseQuickAdapter<AnimeDescBean, BaseViewHolder> {
    private Context context;

    public DramaAdapter(Context context, @Nullable List<AnimeDescBean> data) {
        super(R.layout.item_btn, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, AnimeDescBean item) {
        String title = item.getTitle();
        helper.setText(R.id.tag_group, title.replaceAll("第", "").replaceAll("集", ""));
        if (item.isSelect())
            helper.getView(R.id.tag_group).setBackground(context.getResources().getDrawable(R.drawable.button_selected, null));
        else
            helper.getView(R.id.tag_group).setBackground(context.getResources().getDrawable(R.drawable.button_default, null));
    }
}
