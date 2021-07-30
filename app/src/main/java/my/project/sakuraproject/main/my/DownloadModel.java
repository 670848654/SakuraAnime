package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.DownloadBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.util.Utils;

public class DownloadModel extends BaseModel implements DownloadContract.Model {

    @Override
    public void getData(int offset, int limit, DownloadContract.LoadDataCallback callback) {
        List<DownloadBean> list = DatabaseUtil.queryAllDownloads(limit, offset);
        if (list.size() > 0)
            callback.success(list);
        else
            callback.error(Utils.getString(R.string.empty_download));
    }
}
