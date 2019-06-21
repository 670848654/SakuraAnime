package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HomeWekBean;

public class FragmentAdapter extends BaseQuickAdapter<HomeWekBean, BaseViewHolder> {
    private Context context;

    public FragmentAdapter(Context context, List<HomeWekBean> data) {
        super(R.layout.item_home_week, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, HomeWekBean item) {
//        helper.addOnClickListener(R.id.drama);
        helper.setText(R.id.title, item.getTitle());
        helper.setText(R.id.drama, item.getDrama());
    }
}
