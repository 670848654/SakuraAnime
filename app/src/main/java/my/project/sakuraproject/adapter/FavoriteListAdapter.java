package my.project.sakuraproject.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.util.Utils;

/**
 * 追番列表适配器
 */
public class FavoriteListAdapter extends BaseQuickAdapter<AnimeListBean, BaseViewHolder> {
    private Context context;

    public FavoriteListAdapter(Context context, List<AnimeListBean> list) {
        super(R.layout.item_anime, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, AnimeListBean item) {
        String imgUrl = item.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        Utils.setCardDefaultBg(context, helper.getView(R.id.card_view), helper.getView(R.id.title));
        Utils.setDefaultImage(context, item.getImg(), item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        TextView source = helper.getView(R.id.source);
        source.setBackground(context.getDrawable(item.getSource() == 1 ? R.drawable.imomoe_bg : R.drawable.yhdm_bg));
        source.setText(Utils.getString(item.getSource() == 1 ? R.string.imomoe : R.string.yhdm));
        source.setVisibility(View.VISIBLE);
        helper.setText(R.id.title, item.getTitle());
        helper.getView(R.id.new_view).setVisibility(item.getState() == 1 ? View.VISIBLE : View.GONE);
    }
}
