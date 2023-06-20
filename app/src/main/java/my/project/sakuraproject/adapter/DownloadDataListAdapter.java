package my.project.sakuraproject.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.jzvd.JZUtils;
import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.util.Utils;

/**
 * 下载剧集适配器
 */
public class DownloadDataListAdapter extends BaseQuickAdapter<DownloadDataBean, BaseViewHolder> {
    private Context context;

    public DownloadDataListAdapter(Context context, List<DownloadDataBean> list) {
        super(R.layout.item_download_data, list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, DownloadDataBean item) {
        String imgUrl = item.getAnimeImg();
        ImageView imageView = helper.getView(R.id.img);
        imageView.setTag(R.id.imageid, imgUrl);
        helper.setText(R.id.title, item.getPlayNumber());
        helper.setText(R.id.file_size, item.getFileSize() != 0 ? Utils.getNetFileSizeDescription(item.getFileSize()) : "0B");
        helper.setVisible(R.id.bottom_progress, false);
        String completeText = "";
        switch (item.getComplete()) {
            case 0:
                completeText = "<font color='#1E9FFF'>等待下载</font>";
                break;
            case 1:
                completeText = "<font color='#5FB878'>下载成功</font>";
                break;
            case 2:
                completeText = "<font color='#FF5722'>下载失败</font>";
                break;
        }
        ProgressBar progressBar = helper.getView(R.id.show_progress);
        if (item.getDuration() != 0 && item.getProgress() != 0) {
//            DecimalFormat df1 = new DecimalFormat("##%");
//            helper.setText(R.id.progress_info, "已观看"+df1.format(((double)item.getProgress()/(double)item.getDuration())));
            helper.setText(R.id.time, JZUtils.stringForTime(item.getProgress()) + "/" + JZUtils.stringForTime(item.getDuration()));
            helper.getView(R.id.time).setVisibility(View.VISIBLE);
            progressBar.setMax((int) item.getDuration());
            progressBar.setProgress((int) item.getProgress());
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            helper.getView(R.id.time).setVisibility(View.GONE);
        }
        helper.setText(R.id.state, Html.fromHtml(completeText));
        if (item.getComplete() == 1 && !item.getPath().contains(".m3u8")) {
            helper.getView(R.id.img_box).setBackground(null);
            helper.setText(R.id.number, "");
//            Utils.loadVideoScreenshot(context, item.getPath(), item.getAnimeImg(), helper.getView(R.id.img), 6500 * 1000);
            Utils.loadVideoScreenshot(context, item.getPath(), item.getAnimeImg(), helper.getView(R.id.img), (item.getProgress() == 0 ? 1000 : item.getProgress()) * 1000);
        } else {
            helper.setBackgroundColor(R.id.img_box, R.drawable.download_img_gradient);
            Utils.setImgViewBg(context, item.getSource(), item.getAnimeImg(), "", imageView);
        }
    }
}
