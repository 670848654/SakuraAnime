package my.project.sakuraproject.adapter;

import android.content.Context;

import androidx.cardview.widget.CardView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HomeWekBean;

public class WeekAdapter extends BaseQuickAdapter<HomeWekBean, BaseViewHolder> {
    private Context context;

    public WeekAdapter(Context context, List<HomeWekBean> data) {
        super(R.layout.item_home_week, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, HomeWekBean item) {
//        helper.addOnClickListener(R.id.drama);
        CardView cardView = helper.getView(R.id.card_view);
        cardView.setCardBackgroundColor(context.getResources().getColor(R.color.window_bg));
        helper.setText(R.id.title, item.getTitle());
        helper.setTextColor(R.id.title, context.getResources().getColor(R.color.text_color_primary));
        helper.setText(R.id.drama, item.getDrama());
    }
}
