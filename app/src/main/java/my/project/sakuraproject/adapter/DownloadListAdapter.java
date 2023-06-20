package my.project.sakuraproject.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
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
    private static String NOT_COMPLETE_TEXT = "<br><font color='RED'>其中有 %s 个未完成</font>";

    public DownloadListAdapter(Context context, List<DownloadBean> list) {
        super(R.layout.item_download, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, DownloadBean item) {
        String imgUrl = item.getImgUrl();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        helper.setText(R.id.title, item.getAnimeTitle());
        String imgContent = String.format(Utils.getString(R.string.download_anime_list_content_title), item.getDownloadDataSize());
        if (item.getNoCompleteSize() > 0)
            imgContent += String.format(NOT_COMPLETE_TEXT, item.getNoCompleteSize());
        helper.setText(R.id.number, Html.fromHtml(imgContent));
        Utils.setImgViewBg(context, item.getSource(), item.getImgUrl(), item.getDescUrl(), imageView);
        TextView source = helper.getView(R.id.source);
        boolean isImomoe = item.getSource() == 1;
        source.setBackground(context.getDrawable(isImomoe ? R.drawable.imomoe_bg : R.drawable.yhdm_bg));
        source.setText(Utils.getString(isImomoe ? R.string.imomoe : R.string.yhdm));
        source.setVisibility(View.VISIBLE);
        helper.setVisible(R.id.file_size, false);
        helper.setVisible(R.id.bottom_progress, false);
        helper.setText(R.id.all_size, String.format(Utils.getString(R.string.download_anime_list_size), item.getFilesSize()));
    }
}
