package my.project.sakuraproject.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.WebviewBean;

public class WebviewAdapter extends BaseQuickAdapter<WebviewBean, BaseViewHolder> {
    private Context context;

    public WebviewAdapter(Context context, List<WebviewBean> list) {
        super(R.layout.item_webview, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, WebviewBean item) {
        MaterialButton materialButton = helper.getView(R.id.btn);
        helper.setText(R.id.btn, item.getTitle());
        if (item.isSelect())
            materialButton.setTextColor(context.getResources().getColor(R.color.tabSelectedTextColor));
        else
            materialButton.setTextColor(context.getResources().getColor(R.color.text_color_primary));
    }
}
