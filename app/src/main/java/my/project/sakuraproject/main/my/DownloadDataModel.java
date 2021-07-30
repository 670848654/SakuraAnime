package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;

public class DownloadDataModel extends BaseModel implements DownloadDataContract.Model {

    @Override
    public void getData(String downloadId, int offset, int limit, DownloadDataContract.LoadDataCallback callback) {
        List<DownloadDataBean> list = DatabaseUtil.queryDownloadDataByDownloadId(downloadId, limit, offset);
        callback.success(list);
    }
}
