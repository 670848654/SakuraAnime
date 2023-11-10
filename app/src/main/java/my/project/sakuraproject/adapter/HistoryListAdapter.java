package my.project.sakuraproject.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.jzvd.JZUtils;
import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HistoryBean;
import my.project.sakuraproject.util.Utils;

/**
 * 历史记录列表适配器
 */
public class HistoryListAdapter extends BaseQuickAdapter<HistoryBean, BaseViewHolder> {
    private Context context;

    public HistoryListAdapter(Context context, List<HistoryBean> list) {
        super(R.layout.item_history, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, HistoryBean item) {
        String imgUrl = item.getImgUrl();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        helper.addOnClickListener(R.id.desc_view).addOnClickListener(R.id.delete_view);
        Utils.setDefaultImage(context, item.getImgUrl(), item.getDescUrl(), imageView, false, null, helper.getView(R.id.title));
        helper.setText(R.id.title, item.getTitle());
        TextView source = helper.getView(R.id.source);
        boolean isImomoe = item.getSource() == 1;
        source.setBackground(context.getDrawable(isImomoe ? R.drawable.imomoe_bg : R.drawable.yhdm_bg));
        source.setText(Utils.getString(isImomoe ? R.string.imomoe : R.string.yhdm));
        source.setVisibility(View.VISIBLE);
        helper.setText(R.id.play_date, item.getUpdateTime());
        if (item.getProgress() == 0 && item.getDuration() == 0)
            helper.setText(R.id.time, "播放错误");
        else
            helper.setText(R.id.time, item.getProgress() == 0 ? "已看完" : JZUtils.stringForTime(item.getProgress()) + "/" + JZUtils.stringForTime(item.getDuration()));
        helper.setText(R.id.info, item.getDramaNumber() + (isImomoe ? " | 播放源"+ (item.getPlaySource()+1) : ""));
//        helper.setText(R.id.info, item.getDramaNumber());
        ProgressBar progressBar = helper.getView(R.id.bottom_progress);
        progressBar.setVisibility(item.getProgress() == 0 ? View.GONE : View.VISIBLE);
        progressBar.setMax((int) item.getDuration());
        progressBar.setProgress((int) item.getProgress());
    }
}
