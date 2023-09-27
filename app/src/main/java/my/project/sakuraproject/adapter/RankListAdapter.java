package my.project.sakuraproject.adapter;

import android.content.Context;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.SiliSiliRankBean;

/**
 * SiliSili排行榜适配器
 */
public class RankListAdapter extends BaseQuickAdapter<SiliSiliRankBean.RankItem, BaseViewHolder> {
    private Context context;

    public RankListAdapter(Context context, List<SiliSiliRankBean.RankItem> list) {
        super(R.layout.item_rank, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, SiliSiliRankBean.RankItem item) {
        TextView indexView = helper.getView(R.id.index);
        helper.setText(R.id.index, item.getIndex());
        switch (item.getIndex()) {
            case "1":
                indexView.setBackground(context.getResources().getDrawable(R.drawable.rank_one));
                break;
            case "2":
                indexView.setBackground(context.getResources().getDrawable(R.drawable.rank_two));
                break;
            case "3":
                indexView.setBackground(context.getResources().getDrawable(R.drawable.rank_three));
                break;
            default:
                indexView.setBackground(context.getResources().getDrawable(R.drawable.rank_other));
                break;
        }
        helper.setText(R.id.title, item.getTitle());
        // 站点好像是反的。。。手动调整
        helper.setText(R.id.score, item.getHot());
        helper.setText(R.id.hot, item.getScore());
    }
}
