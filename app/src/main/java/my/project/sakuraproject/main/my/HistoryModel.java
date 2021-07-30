package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.HistoryBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.util.Utils;

public class HistoryModel extends BaseModel implements HistoryContract.Model {

    @Override
    public void getData(int offset, int limit, HistoryContract.LoadDataCallback callback) {
        List<HistoryBean> list = DatabaseUtil.queryAllHistory(limit, offset);
        if (list.size() > 0)
            callback.success(list);
        else
            callback.error(Utils.getString(R.string.empty_history));
    }
}
