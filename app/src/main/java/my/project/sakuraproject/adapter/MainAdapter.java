package my.project.sakuraproject.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.MainBean;


/**
 * 我的列表适配器
 */
public class MainAdapter extends BaseQuickAdapter<MainBean, BaseViewHolder> {
    private Context context;

    public MainAdapter(Context context, List<MainBean> list) {
        super(R.layout.item_main, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, MainBean bean) {
        helper.setBackgroundColor(R.id.card, context.getResources().getColor(R.color.window_bg));
        helper.setTextColor(R.id.title, context.getResources().getColor(R.color.text_color_primary));
        ImageView img = helper.getView(R.id.img);
        img.setColorFilter(context.getResources().getColor(R.color.text_color_primary));
        helper.setText(R.id.title, bean.getTitle());
        helper.setImageDrawable(R.id.img, context.getDrawable(bean.getImg()));
        if (bean.getNumber() == 0)
            helper.getView(R.id.number).setVisibility(View.GONE);
        else
            helper.setText(R.id.number, bean.getNumber() >= 99 ? "99+" : bean.getNumber()+"");
    }
}