package my.project.sakuraproject.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.DownloadBean;
import my.project.sakuraproject.util.Utils;

/**
 * 下载列表适配器
 */
public class DownloadListAdapter extends BaseQuickAdapter<DownloadBean, BaseViewHolder> {
    private Context context;

    public DownloadListAdapter(Context context, List<DownloadBean> list) {
        super(R.layout.item_download, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, DownloadBean item) {
        helper.setText(R.id.title, item.getAnimeTitle());
        String imgContent = "共 " +item.getDownloadDataSize() + " 个内容";
        if (item.getNoCompleteSize() > 0)
            imgContent += "<br><font color='RED'>其中有 " + item.getNoCompleteSize() + " 个未完成</font>";
        helper.setText(R.id.number, Html.fromHtml(imgContent));
        Utils.setImgViewBg(context, item.getSource(), item.getImgUrl(), helper.getView(R.id.img));
        TextView source = helper.getView(R.id.source);
        boolean isImomoe = item.getSource() == 1;
        source.setBackground(context.getDrawable(isImomoe ? R.drawable.imomoe_bg : R.drawable.yhdm_bg));
        source.setText(Utils.getString(isImomoe ? R.string.imomoe : R.string.yhdm));
        source.setVisibility(View.VISIBLE);
        helper.setVisible(R.id.file_size, false);
        helper.setVisible(R.id.bottom_progress, false);
        helper.setText(R.id.all_size, "占用:" + item.getFilesSize());
    }
}
