package my.project.sakuraproject.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.DownloadDramaBean;

/**
 * 下载选集列表适配器
 */
public class DownloadDramaAdapter extends BaseQuickAdapter<DownloadDramaBean, BaseViewHolder> {
    private Context context;

    public DownloadDramaAdapter(Context context, @Nullable List<DownloadDramaBean> data) {
        super(R.layout.item_desc_drama, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, DownloadDramaBean item) {
        MaterialButton materialButton = helper.getView(R.id.tag_group);
        helper.setText(R.id.tag_group, item.getTitle());
        if (item.isSelected())
            materialButton.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
        else
            materialButton.setTextColor(context.getResources().getColor(R.color.text_color_primary));
        if (item.isHasDownload())
            materialButton.setTextColor(context.getResources().getColor(R.color.green500));
        if (item.isShouldParse())
            materialButton.setTextColor(context.getResources().getColor(R.color.red500));
    }
}