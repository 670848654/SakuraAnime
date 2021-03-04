package my.project.sakuraproject.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.util.Utils;

/**
 * 相关推荐适配器
 */
public class AnimeDescRecommendAdapter extends BaseQuickAdapter<AnimeDescRecommendBean, BaseViewHolder> {
    private Context context;

    public AnimeDescRecommendAdapter(Context context, @Nullable List<AnimeDescRecommendBean> data) {
        super(R.layout.item_desc_recommend, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, AnimeDescRecommendBean item) {
        Utils.setCardDefaultBg(context, helper.getView(R.id.card_view), helper.getView(R.id.title));
        helper.setText(R.id.title, item.getTitle());
        Utils.setDefaultImage(context, item.getImg(), helper.getView(R.id.img), true, helper.getView(R.id.card_view), helper.getView(R.id.title));
//        Utils.setCardBg(context, item.getImg(), helper.getView(R.id.card_view), helper.getView(R.id.title));
    }
}