package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.util.Utils;

public class AnimeListAdapter extends BaseQuickAdapter<AnimeListBean, BaseViewHolder> {
    private Context context;

    public AnimeListAdapter(Context context, List list) {
        super(R.layout.item_anime, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, AnimeListBean item) {
        Utils.setCardDefaultBg(context, helper.getView(R.id.card_view), helper.getView(R.id.title));
        Utils.setDefaultImage(context, item.getImg(), helper.getView(R.id.img));
        helper.setText(R.id.title, item.getTitle());
        Utils.setCardBg(context, item.getImg(), helper.getView(R.id.card_view), helper.getView(R.id.title));
    }
}
