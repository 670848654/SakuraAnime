package my.project.sakuraproject.adapter;

import android.content.Context;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

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
        Button button = helper.getView(R.id.btn);
        helper.setText(R.id.btn, item.getTitle());
        button.setTextColor(context.getResources().getColor(R.color.text_color_primary));
        button.setBackgroundResource(R.drawable.button_default);
        if (item.isSelect()){
            button.setTextColor(context.getResources().getColor(R.color.item_selected_color));
            button.setBackgroundResource(R.drawable.button_selected);
        } else {
            button.setTextColor(context.getResources().getColor(R.color.text_color_primary));
            button.setBackgroundResource(R.drawable.button_default);
        }
    }
}
