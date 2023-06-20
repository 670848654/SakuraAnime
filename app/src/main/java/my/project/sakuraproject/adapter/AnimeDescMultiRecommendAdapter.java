package my.project.sakuraproject.adapter;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.util.Utils;

/**
 * 多季、相关推荐适配器
 */
public class AnimeDescMultiRecommendAdapter extends BaseQuickAdapter<AnimeDescRecommendBean, BaseViewHolder> {
    private Context context;

    public AnimeDescMultiRecommendAdapter(Context context, @Nullable List<AnimeDescRecommendBean> data) {
        super(R.layout.item_desc_recommend, data);
        this.context = context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, AnimeDescRecommendBean item) {
        String imgUrl = item.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        Utils.setCardDefaultBg(context, helper.getView(R.id.card_view), helper.getView(R.id.title));
        Utils.setDefaultImage(context, item.getImg(), item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        helper.setText(R.id.title, item.getTitle());
    }
}