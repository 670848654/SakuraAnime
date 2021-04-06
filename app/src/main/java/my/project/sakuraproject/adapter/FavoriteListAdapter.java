package my.project.sakuraproject.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.util.Utils;

public class FavoriteListAdapter extends BaseQuickAdapter<AnimeListBean, BaseViewHolder> {
    private Context context;

    public FavoriteListAdapter(Context context, List<AnimeListBean> list) {
        super(R.layout.item_anime, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, AnimeListBean item) {
        Utils.setCardDefaultBg(context, helper.getView(R.id.card_view), helper.getView(R.id.title));
        Utils.setDefaultImage(context, item.getImg(), item.getUrl(), helper.getView(R.id.img), true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        TextView source = helper.getView(R.id.source);
        source.setBackground(context.getDrawable(item.getUrl().contains("/view/") ? R.drawable.imomoe_bg : R.drawable.yhdm_bg));
        source.setText(Utils.getString(item.getUrl().contains("/view/") ? R.string.imomoe : R.string.yhdm));
        source.setVisibility(View.VISIBLE);
        helper.setText(R.id.title, item.getTitle().replaceAll("imomoe", ""));
    }
}
