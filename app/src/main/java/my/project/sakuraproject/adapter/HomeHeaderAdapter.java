package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HomeHeaderBean;

public class HomeHeaderAdapter extends BaseQuickAdapter<HomeHeaderBean.HeaderDataBean, BaseViewHolder> {
    private Context context;
    public HomeHeaderAdapter(Context context, List<HomeHeaderBean.HeaderDataBean> data) {
        super(R.layout.item_home_header_data, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, HomeHeaderBean.HeaderDataBean item) {
        helper.setBackgroundColor(R.id.root, context.getResources().getColor(R.color.window_bg));
        helper.setText(R.id.title, item.getTitle());
        helper.setTextColor(R.id.title, context.getResources().getColor(R.color.text_color_primary));
        helper.setImageDrawable(R.id.img, context.getDrawable(item.getImg()));
    }
}
