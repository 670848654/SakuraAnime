package my.project.sakuraproject.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.util.Utils;

/**
 * 番剧列表适配器
 */
public class AnimeListAdapter extends BaseQuickAdapter<AnimeListBean, BaseViewHolder> {
    private Context context;

    public AnimeListAdapter(Context context, List list, boolean isTopic) {
        super(isTopic ? R.layout.item_topic : R.layout.item_anime, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, AnimeListBean item) {
        String imgUrl = item.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        Utils.setCardDefaultBg(context, helper.getView(R.id.card_view), helper.getView(R.id.title));
        Utils.setDefaultImage(context, item.getImg(), item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        helper.setText(R.id.title, item.getTitle());
    }
}
