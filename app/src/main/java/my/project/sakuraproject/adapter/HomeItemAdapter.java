package my.project.sakuraproject.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.util.Utils;

public class HomeItemAdapter extends BaseQuickAdapter<HomeBean.HomeItemBean, BaseViewHolder> {
    private Context context;

    public HomeItemAdapter(Context context, List<HomeBean.HomeItemBean> data) {
        super(R.layout.item_home_data, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, HomeBean.HomeItemBean item) {
        String imgUrl = item.getImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        helper.getView(R.id.episodes).setVisibility(View.VISIBLE);
        helper.setText(R.id.update_time, item.getEpisodes().isEmpty() ? "NULL" : item.getEpisodes());
        Utils.setCardDefaultBg(context, helper.getView(R.id.card_view), helper.getView(R.id.title));
        Utils.setDefaultImage(context, item.getImg(), item.getUrl(), imageView, true, helper.getView(R.id.card_view), helper.getView(R.id.title));
        helper.setText(R.id.title, item.getTitle());
    }
}
