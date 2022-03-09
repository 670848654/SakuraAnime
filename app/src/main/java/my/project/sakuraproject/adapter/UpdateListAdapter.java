package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeUpdateBean;

/**
 * 番剧列表适配器
 */
public class UpdateListAdapter extends BaseQuickAdapter<AnimeUpdateBean, BaseViewHolder> {
    private Context context;

    public UpdateListAdapter(Context context, List<AnimeUpdateBean> list) {
        super(R.layout.item_update, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, AnimeUpdateBean item) {
        helper.setText(R.id.number, item.getNumber());
        helper.setText(R.id.title, item.getTitle());
        helper.setText(R.id.region, item.getRegion());
        helper.setText(R.id.episodes, item.getEpisodes());
        helper.setText(R.id.update_time, item.getUpdateTime());
    }
}
